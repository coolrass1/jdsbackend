package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.DocumentSignatureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSignatureResponse {
    private Long id;
    private Long documentId;
    private String documentName;
    private Long signerId;
    private String signerUsername;
    private DocumentSignatureStatus status;
    private String signatureToken;
    private LocalDateTime signedAt;
    private LocalDateTime expiresAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
