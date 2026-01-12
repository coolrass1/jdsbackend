package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.DocumentVersionResponse;
import com.skk.jdsbackend.service.DocumentVersionService;
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
@RequestMapping("/api/documents/versions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentVersionController {

    private final DocumentVersionService versionService;

    /**
     * POST /api/documents/versions/{documentId}
     * Create a new version of a document
     */
    @PostMapping("/{documentId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentVersionResponse> createNewVersion(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "changeDescription", required = false) String changeDescription,
            Authentication authentication) {
        
        String username = authentication.getName();
        DocumentVersionResponse response = versionService.createNewVersion(
                documentId, file, changeDescription, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * GET /api/documents/versions/{documentId}
     * Get all versions of a document
     */
    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<DocumentVersionResponse>> getDocumentVersions(@PathVariable Long documentId) {
        List<DocumentVersionResponse> versions = versionService.getDocumentVersions(documentId);
        return ResponseEntity.ok(versions);
    }

    /**
     * GET /api/documents/versions/version/{versionId}
     * Get a specific version by ID
     */
    @GetMapping("/version/{versionId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentVersionResponse> getVersionById(@PathVariable Long versionId) {
        DocumentVersionResponse response = versionService.getVersionById(versionId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/documents/versions/download/{versionId}
     * Download a specific version
     */
    @GetMapping("/download/{versionId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadVersion(@PathVariable Long versionId) {
        DocumentVersionResponse versionInfo = versionService.getVersionById(versionId);
        Resource resource = versionService.downloadVersion(versionId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(versionInfo.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + versionInfo.getFileName() + "\"")
                .body(resource);
    }

    /**
     * POST /api/documents/versions/{documentId}/restore/{versionNumber}
     * Restore a specific version (make it the current version)
     */
    @PostMapping("/{documentId}/restore/{versionNumber}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentVersionResponse> restoreVersion(
            @PathVariable Long documentId,
            @PathVariable Integer versionNumber,
            Authentication authentication) {
        
        String username = authentication.getName();
        DocumentVersionResponse response = versionService.restoreVersion(documentId, versionNumber, username);
        return ResponseEntity.ok(response);
    }
}
