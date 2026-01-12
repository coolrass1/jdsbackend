package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.DocumentVersionResponse;
import com.skk.jdsbackend.entity.Document;
import com.skk.jdsbackend.entity.DocumentVersion;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.DocumentRepository;
import com.skk.jdsbackend.repository.DocumentVersionRepository;
import com.skk.jdsbackend.repository.UserRepository;
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
public class DocumentVersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Create a new version of an existing document
     */
    @Transactional
    public DocumentVersionResponse createNewVersion(Long documentId, MultipartFile file, 
                                                    String changeDescription, String username) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Get the latest version number
        Integer newVersionNumber = document.getCurrentVersion() + 1;

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // Generate unique filename for the new version
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the new version file
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create version record
            DocumentVersion version = new DocumentVersion();
            version.setDocument(document);
            version.setVersionNumber(newVersionNumber);
            version.setFileName(originalFileName);
            version.setFilePath(filePath.toString());
            version.setFileType(file.getContentType());
            version.setFileSize(file.getSize());
            version.setUploadedBy(user);
            version.setChangeDescription(changeDescription);

            DocumentVersion savedVersion = versionRepository.save(version);

            // Update the document with the new version info
            document.setCurrentVersion(newVersionNumber);
            document.setFileName(originalFileName);
            document.setFilePath(filePath.toString());
            document.setFileType(file.getContentType());
            document.setFileSize(file.getSize());
            documentRepository.save(document);

            return mapToResponse(savedVersion);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store new version: " + originalFileName, e);
        }
    }

    /**
     * Get all versions of a document
     */
    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> getDocumentVersions(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        return versionRepository.findByDocumentOrderByVersionNumberDesc(document).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific version of a document
     */
    @Transactional(readOnly = true)
    public DocumentVersionResponse getVersionById(Long versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found with id: " + versionId));
        return mapToResponse(version);
    }

    /**
     * Download a specific version of a document
     */
    @Transactional(readOnly = true)
    public Resource downloadVersion(Long versionId) {
        DocumentVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found with id: " + versionId));

        try {
            Path filePath = Paths.get(version.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable: " + version.getFileName());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + version.getFileName(), e);
        }
    }

    /**
     * Restore a specific version of a document (make it the current version)
     */
    @Transactional
    public DocumentVersionResponse restoreVersion(Long documentId, Integer versionNumber, String username) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        DocumentVersion versionToRestore = versionRepository.findByDocumentAndVersionNumber(document, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Version " + versionNumber + " not found for document id: " + documentId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        // Create a new version based on the restored version
        Integer newVersionNumber = document.getCurrentVersion() + 1;

        try {
            // Copy the file from the version to restore
            Path sourcePath = Paths.get(versionToRestore.getFilePath());
            String fileExtension = "";
            if (versionToRestore.getFileName().contains(".")) {
                fileExtension = versionToRestore.getFileName()
                        .substring(versionToRestore.getFileName().lastIndexOf("."));
            }
            
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path uploadPath = Paths.get(uploadDir);
            Path targetPath = uploadPath.resolve(uniqueFileName);

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Create new version record
            DocumentVersion newVersion = new DocumentVersion();
            newVersion.setDocument(document);
            newVersion.setVersionNumber(newVersionNumber);
            newVersion.setFileName(versionToRestore.getFileName());
            newVersion.setFilePath(targetPath.toString());
            newVersion.setFileType(versionToRestore.getFileType());
            newVersion.setFileSize(versionToRestore.getFileSize());
            newVersion.setUploadedBy(user);
            newVersion.setChangeDescription("Restored from version " + versionNumber);

            DocumentVersion savedVersion = versionRepository.save(newVersion);

            // Update document with restored version info
            document.setCurrentVersion(newVersionNumber);
            document.setFileName(versionToRestore.getFileName());
            document.setFilePath(targetPath.toString());
            document.setFileType(versionToRestore.getFileType());
            document.setFileSize(versionToRestore.getFileSize());
            documentRepository.save(document);

            return mapToResponse(savedVersion);

        } catch (IOException e) {
            throw new RuntimeException("Failed to restore version: " + versionNumber, e);
        }
    }

    private DocumentVersionResponse mapToResponse(DocumentVersion version) {
        return DocumentVersionResponse.builder()
                .id(version.getId())
                .documentId(version.getDocument().getId())
                .versionNumber(version.getVersionNumber())
                .fileName(version.getFileName())
                .fileType(version.getFileType())
                .fileSize(version.getFileSize())
                .uploadedByUsername(version.getUploadedBy() != null ? 
                        version.getUploadedBy().getUsername() : null)
                .changeDescription(version.getChangeDescription())
                .createdAt(version.getCreatedAt())
                .build();
    }
}
