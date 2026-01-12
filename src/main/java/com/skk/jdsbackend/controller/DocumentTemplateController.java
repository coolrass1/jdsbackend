package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.DocumentTemplateCreateRequest;
import com.skk.jdsbackend.dto.DocumentTemplateResponse;
import com.skk.jdsbackend.dto.MessageResponse;
import com.skk.jdsbackend.service.DocumentTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentTemplateController {

    private final DocumentTemplateService templateService;

    /**
     * POST /api/documents/templates
     * Create a new document template
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DocumentTemplateResponse> createTemplate(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        DocumentTemplateCreateRequest request = new DocumentTemplateCreateRequest(name, description, category);
        String username = authentication.getName();
        DocumentTemplateResponse response = templateService.createTemplate(request, file, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/documents/templates
     * Get all active templates
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentTemplateResponse>> getAllActiveTemplates() {
        List<DocumentTemplateResponse> templates = templateService.getAllActiveTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/documents/templates/category/{category}
     * Get templates by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentTemplateResponse>> getTemplatesByCategory(@PathVariable String category) {
        List<DocumentTemplateResponse> templates = templateService.getTemplatesByCategory(category);
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/documents/templates/{id}
     * Get template by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentTemplateResponse> getTemplateById(@PathVariable Long id) {
        DocumentTemplateResponse response = templateService.getTemplateById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/documents/templates/download/{id}
     * Download template file
     */
    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadTemplate(@PathVariable Long id) {
        DocumentTemplateResponse templateInfo = templateService.getTemplateById(id);
        Resource resource = templateService.downloadTemplate(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(templateInfo.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + templateInfo.getName() + "\"")
                .body(resource);
    }

    /**
     * PUT /api/documents/templates/{id}
     * Update template details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DocumentTemplateResponse> updateTemplate(
            @PathVariable Long id,
            @RequestBody DocumentTemplateCreateRequest request) {
        DocumentTemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/documents/templates/{id}/deactivate
     * Deactivate a template
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deactivateTemplate(@PathVariable Long id) {
        templateService.deactivateTemplate(id);
        return ResponseEntity.ok(new MessageResponse("Template deactivated successfully"));
    }

    /**
     * DELETE /api/documents/templates/{id}
     * Delete a template
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(new MessageResponse("Template deleted successfully"));
    }
}
