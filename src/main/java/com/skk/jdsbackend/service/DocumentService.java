package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.DocumentResponse;
import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.Document;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final ActivityService activityService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public DocumentResponse uploadDocument(Long caseId, MultipartFile file) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + caseId));

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generate unique filename
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create document entity
            Document document = new Document();
            document.setFileName(originalFileName);
            document.setFilePath(filePath.toString());
            document.setFileType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setCaseEntity(caseEntity);

            Document savedDocument = documentRepository.save(document);

            // Log activity
            activityService.logActivity(
                    "document_uploaded",
                    "DOCUMENT",
                    savedDocument.getId(),
                    caseId,
                    String.format("Uploaded document: %s (%.2f KB)", originalFileName, file.getSize() / 1024.0));

            return mapToResponse(savedDocument);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFileName, e);
        }
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return mapToResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByCaseId(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + caseId));
        return documentRepository.findByCaseEntityOrderByUploadedAtDesc(caseEntity).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        try {
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + document.getFileName());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + document.getFileName(), e);
        }
    }

    @Transactional
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));

        String fileName = document.getFileName();
        Long caseId = document.getCaseEntity() != null ? document.getCaseEntity().getId() : null;

        try {
            // Delete file from filesystem
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete document record
            documentRepository.deleteById(id);

            // Log activity
            activityService.logActivity(
                    "document_deleted",
                    "DOCUMENT",
                    id,
                    caseId,
                    String.format("Deleted document: %s", fileName));

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + document.getFileName(), e);
        }
    }

    private DocumentResponse mapToResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        response.setId(document.getId());
        response.setFileName(document.getFileName());
        response.setFileType(document.getFileType());
        response.setFileSize(document.getFileSize());
        response.setCaseId(document.getCaseEntity().getId());
        response.setCurrentVersion(document.getCurrentVersion());
        response.setDescription(document.getDescription());
        response.setOcrText(document.getOcrText());
        response.setIsTemplateBased(document.getIsTemplateBased());

        if (document.getTemplate() != null) {
            response.setTemplateId(document.getTemplate().getId());
            response.setTemplateName(document.getTemplate().getName());
        }

        if (document.getUploadedBy() != null) {
            response.setUploadedByUsername(document.getUploadedBy().getUsername());
        }

        response.setTotalVersions(document.getVersions().size());

        long pendingSignatures = document.getSignatures().stream()
                .filter(sig -> sig.getStatus() == com.skk.jdsbackend.entity.DocumentSignatureStatus.PENDING)
                .count();
        response.setPendingSignatures((int) pendingSignatures);

        response.setUploadedAt(document.getUploadedAt());
        response.setUpdatedAt(document.getUpdatedAt());
        return response;
    }

    /**
     * Handle callback from ONLYOFFICE Document Server when document is saved
     */
    @Transactional
    public void handleOnlyOfficeCallback(Long documentId, com.skk.jdsbackend.dto.OnlyOfficeCallbackRequest callback) {
        // Status 2 means document is ready to be saved
        // Status 3 means document saving error
        if (callback.getStatus() != 2) {
            // Ignore other statuses (1 = editing, 4 = closed with no changes, etc.)
            return;
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        try {
            // Download the saved document from ONLYOFFICE callback URL
            java.net.URI uri = new java.net.URI(callback.getUrl());
            java.io.InputStream inputStream = uri.toURL().openStream();

            // Save the updated file
            Path filePath = Paths.get(document.getFilePath());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            inputStream.close();

            // Update document version
            document.setCurrentVersion(document.getCurrentVersion() + 1);
            documentRepository.save(document);

            // Log activity
            activityService.logActivity(
                    "document_edited",
                    "DOCUMENT",
                    documentId,
                    document.getCaseEntity().getId(),
                    String.format("Document edited via ONLYOFFICE: %s (v%d)",
                            document.getFileName(), document.getCurrentVersion()));

        } catch (Exception e) {
            throw new RuntimeException("Failed to save document from ONLYOFFICE callback", e);
        }
    }
}
