package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.DocumentTemplateCreateRequest;
import com.skk.jdsbackend.dto.DocumentTemplateResponse;
import com.skk.jdsbackend.entity.DocumentTemplate;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.DocumentTemplateRepository;
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
public class DocumentTemplateService {

    private final DocumentTemplateRepository templateRepository;
    private final UserRepository userRepository;

    @Value("${file.template-dir:templates}")
    private String templateDir;

    /**
     * Create a new document template
     */
    @Transactional
    public DocumentTemplateResponse createTemplate(DocumentTemplateCreateRequest request, 
                                                   MultipartFile file, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path templatePath = Paths.get(templateDir);
            if (!Files.exists(templatePath)) {
                Files.createDirectories(templatePath);
            }

            Path filePath = templatePath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            DocumentTemplate template = new DocumentTemplate();
            template.setName(request.getName());
            template.setDescription(request.getDescription());
            template.setFilePath(filePath.toString());
            template.setFileType(file.getContentType());
            template.setCategory(request.getCategory());
            template.setCreatedBy(user);
            template.setIsActive(true);

            DocumentTemplate savedTemplate = templateRepository.save(template);
            return mapToResponse(savedTemplate);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store template file: " + originalFileName, e);
        }
    }

    /**
     * Get all active templates
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateResponse> getAllActiveTemplates() {
        return templateRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get templates by category
     */
    @Transactional(readOnly = true)
    public List<DocumentTemplateResponse> getTemplatesByCategory(String category) {
        return templateRepository.findByIsActiveTrueAndCategoryOrderByNameAsc(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get template by ID
     */
    @Transactional(readOnly = true)
    public DocumentTemplateResponse getTemplateById(Long id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        return mapToResponse(template);
    }

    /**
     * Download template file
     */
    @Transactional(readOnly = true)
    public Resource downloadTemplate(Long id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        try {
            Path filePath = Paths.get(template.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Template file not found or not readable: " + template.getName());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Template file not found: " + template.getName(), e);
        }
    }

    /**
     * Update template (activate/deactivate)
     */
    @Transactional
    public DocumentTemplateResponse updateTemplate(Long id, DocumentTemplateCreateRequest request) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }

        DocumentTemplate updatedTemplate = templateRepository.save(template);
        return mapToResponse(updatedTemplate);
    }

    /**
     * Deactivate template
     */
    @Transactional
    public void deactivateTemplate(Long id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        template.setIsActive(false);
        templateRepository.save(template);
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(Long id) {
        DocumentTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        try {
            Path filePath = Paths.get(template.getFilePath());
            Files.deleteIfExists(filePath);
            templateRepository.deleteById(id);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete template file: " + template.getName(), e);
        }
    }

    private DocumentTemplateResponse mapToResponse(DocumentTemplate template) {
        return DocumentTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .fileType(template.getFileType())
                .category(template.getCategory())
                .isActive(template.getIsActive())
                .createdByUsername(template.getCreatedBy() != null ? 
                        template.getCreatedBy().getUsername() : null)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
