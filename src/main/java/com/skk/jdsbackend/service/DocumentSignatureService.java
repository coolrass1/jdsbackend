package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.DocumentSignatureRequest;
import com.skk.jdsbackend.dto.DocumentSignatureResponse;
import com.skk.jdsbackend.dto.SignDocumentRequest;
import com.skk.jdsbackend.entity.Document;
import com.skk.jdsbackend.entity.DocumentSignature;
import com.skk.jdsbackend.entity.DocumentSignatureStatus;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.DocumentRepository;
import com.skk.jdsbackend.repository.DocumentSignatureRepository;
import com.skk.jdsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentSignatureService {

    private final DocumentSignatureRepository signatureRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    /**
     * Request a signature for a document
     */
    @Transactional
    public DocumentSignatureResponse requestSignature(Long documentId, DocumentSignatureRequest request) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        User signer = userRepository.findById(request.getSignerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getSignerId()));

        // Generate unique signature token
        String signatureToken = UUID.randomUUID().toString();

        DocumentSignature signature = new DocumentSignature();
        signature.setDocument(document);
        signature.setSigner(signer);
        signature.setStatus(DocumentSignatureStatus.PENDING);
        signature.setSignatureToken(signatureToken);
        signature.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days to sign

        DocumentSignature savedSignature = signatureRepository.save(signature);
        
        // TODO: Send email notification to signer with signature link
        // Email should contain: document name, requester info, and signature link with token
        
        return mapToResponse(savedSignature);
    }

    /**
     * Sign a document using signature token
     */
    @Transactional
    public DocumentSignatureResponse signDocument(SignDocumentRequest request) {
        DocumentSignature signature = signatureRepository.findBySignatureToken(request.getSignatureToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid signature token"));

        // Check if signature is expired
        if (signature.getExpiresAt().isBefore(LocalDateTime.now())) {
            signature.setStatus(DocumentSignatureStatus.EXPIRED);
            signatureRepository.save(signature);
            throw new RuntimeException("Signature request has expired");
        }

        // Check if already signed
        if (signature.getStatus() != DocumentSignatureStatus.PENDING) {
            throw new RuntimeException("Signature request is not pending. Current status: " + signature.getStatus());
        }

        // Update signature
        signature.setStatus(DocumentSignatureStatus.SIGNED);
        signature.setSignatureData(request.getSignatureData());
        signature.setSignedAt(LocalDateTime.now());

        DocumentSignature updatedSignature = signatureRepository.save(signature);
        return mapToResponse(updatedSignature);
    }

    /**
     * Reject a signature request
     */
    @Transactional
    public DocumentSignatureResponse rejectSignature(String signatureToken, String rejectionReason) {
        DocumentSignature signature = signatureRepository.findBySignatureToken(signatureToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid signature token"));

        if (signature.getStatus() != DocumentSignatureStatus.PENDING) {
            throw new RuntimeException("Signature request is not pending. Current status: " + signature.getStatus());
        }

        signature.setStatus(DocumentSignatureStatus.REJECTED);
        signature.setRejectionReason(rejectionReason);

        DocumentSignature updatedSignature = signatureRepository.save(signature);
        return mapToResponse(updatedSignature);
    }

    /**
     * Get all signatures for a document
     */
    @Transactional(readOnly = true)
    public List<DocumentSignatureResponse> getDocumentSignatures(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        return signatureRepository.findByDocumentOrderByCreatedAtDesc(document).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending signatures for a user
     */
    @Transactional(readOnly = true)
    public List<DocumentSignatureResponse> getPendingSignaturesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return signatureRepository.findBySignerAndStatus(user, DocumentSignatureStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get signature by token (for public access)
     */
    @Transactional(readOnly = true)
    public DocumentSignatureResponse getSignatureByToken(String token) {
        DocumentSignature signature = signatureRepository.findBySignatureToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid signature token"));
        return mapToResponse(signature);
    }

    private DocumentSignatureResponse mapToResponse(DocumentSignature signature) {
        return DocumentSignatureResponse.builder()
                .id(signature.getId())
                .documentId(signature.getDocument().getId())
                .documentName(signature.getDocument().getFileName())
                .signerId(signature.getSigner().getId())
                .signerUsername(signature.getSigner().getUsername())
                .status(signature.getStatus())
                .signatureToken(signature.getSignatureToken())
                .signedAt(signature.getSignedAt())
                .expiresAt(signature.getExpiresAt())
                .rejectionReason(signature.getRejectionReason())
                .createdAt(signature.getCreatedAt())
                .build();
    }
}
