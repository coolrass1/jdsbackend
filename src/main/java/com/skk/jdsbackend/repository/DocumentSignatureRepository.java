package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Document;
import com.skk.jdsbackend.entity.DocumentSignature;
import com.skk.jdsbackend.entity.DocumentSignatureStatus;
import com.skk.jdsbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, Long> {
    
    List<DocumentSignature> findByDocumentOrderByCreatedAtDesc(Document document);
    
    List<DocumentSignature> findBySignerOrderByCreatedAtDesc(User signer);
    
    List<DocumentSignature> findByStatus(DocumentSignatureStatus status);
    
    Optional<DocumentSignature> findBySignatureToken(String signatureToken);
    
    List<DocumentSignature> findBySignerAndStatus(User signer, DocumentSignatureStatus status);
}
