package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.DocumentResponse;
import com.skk.jdsbackend.dto.MessageResponse;
import com.skk.jdsbackend.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("caseId") Long caseId,
            @RequestParam("file") MultipartFile file) {
        DocumentResponse response = documentService.uploadDocument(caseId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<DocumentResponse>> getDocumentsByCaseId(@PathVariable Long caseId) {
        List<DocumentResponse> documents = documentService.getDocumentsByCaseId(caseId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        DocumentResponse documentInfo = documentService.getDocumentById(id);
        Resource resource = documentService.downloadDocument(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentInfo.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + documentInfo.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(new MessageResponse("Document deleted successfully"));
    }
}
