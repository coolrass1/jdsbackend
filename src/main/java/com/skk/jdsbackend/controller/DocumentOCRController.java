package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.service.DocumentOCRService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents/ocr")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentOCRController {

    private final DocumentOCRService ocrService;

    /**
     * POST /api/documents/ocr/extract/{documentId}
     * Extract text from document using OCR
     */
    @PostMapping("/extract/{documentId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> extractText(@PathVariable Long documentId) {
        String extractedText = ocrService.extractTextFromDocument(documentId);
        
        Map<String, String> response = new HashMap<>();
        response.put("documentId", documentId.toString());
        response.put("extractedText", extractedText);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/documents/ocr/{documentId}
     * Get OCR text for a document
     */
    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getOCRText(@PathVariable Long documentId) {
        String ocrText = ocrService.getOCRText(documentId);
        
        Map<String, String> response = new HashMap<>();
        response.put("documentId", documentId.toString());
        response.put("ocrText", ocrText);
        
        return ResponseEntity.ok(response);
    }
}
