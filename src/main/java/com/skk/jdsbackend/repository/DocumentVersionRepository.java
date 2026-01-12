package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Document;
import com.skk.jdsbackend.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    
    List<DocumentVersion> findByDocumentOrderByVersionNumberDesc(Document document);
    
    Optional<DocumentVersion> findByDocumentAndVersionNumber(Document document, Integer versionNumber);
    
    Optional<DocumentVersion> findTopByDocumentOrderByVersionNumberDesc(Document document);
}
