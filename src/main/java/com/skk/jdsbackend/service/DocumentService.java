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

        try {
            // Delete file from filesystem
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);

            // Delete document record
            documentRepository.deleteById(id);
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
        response.setUploadedAt(document.getUploadedAt());
        return response;
    }
}
