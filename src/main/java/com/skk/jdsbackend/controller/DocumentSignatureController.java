package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.DocumentSignatureRequest;
import com.skk.jdsbackend.dto.DocumentSignatureResponse;
import com.skk.jdsbackend.dto.SignDocumentRequest;
import com.skk.jdsbackend.service.DocumentSignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents/signatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentSignatureController {

    private final DocumentSignatureService signatureService;

    /**
     * POST /api/documents/signatures/request/{documentId}
     * Request a signature for a document
     */
    @PostMapping("/request/{documentId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<DocumentSignatureResponse> requestSignature(
            @PathVariable Long documentId,
            @RequestBody DocumentSignatureRequest request) {
        DocumentSignatureResponse response = signatureService.requestSignature(documentId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * POST /api/documents/signatures/sign
     * Sign a document using signature token (public endpoint for external access)
     */
    @PostMapping("/sign")
    public ResponseEntity<DocumentSignatureResponse> signDocument(@RequestBody SignDocumentRequest request) {
        DocumentSignatureResponse response = signatureService.signDocument(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/documents/signatures/reject
     * Reject a signature request
     */
    @PostMapping("/reject")
    public ResponseEntity<DocumentSignatureResponse> rejectSignature(
            @RequestParam("token") String token,
            @RequestParam(value = "reason", required = false) String reason) {
        DocumentSignatureResponse response = signatureService.rejectSignature(token, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/documents/signatures/document/{documentId}
     * Get all signatures for a document
     */
    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<DocumentSignatureResponse>> getDocumentSignatures(@PathVariable Long documentId) {
        List<DocumentSignatureResponse> signatures = signatureService.getDocumentSignatures(documentId);
        return ResponseEntity.ok(signatures);
    }

    /**
     * GET /api/documents/signatures/pending/{userId}
     * Get all pending signatures for a user
     */
    @GetMapping("/pending/{userId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<DocumentSignatureResponse>> getPendingSignaturesForUser(@PathVariable Long userId) {
        List<DocumentSignatureResponse> signatures = signatureService.getPendingSignaturesForUser(userId);
        return ResponseEntity.ok(signatures);
    }

    /**
     * GET /api/documents/signatures/token/{token}
     * Get signature details by token (public endpoint for verification)
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<DocumentSignatureResponse> getSignatureByToken(@PathVariable String token) {
        DocumentSignatureResponse response = signatureService.getSignatureByToken(token);
        return ResponseEntity.ok(response);
    }
}
