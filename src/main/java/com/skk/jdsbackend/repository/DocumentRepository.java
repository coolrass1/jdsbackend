package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Document;
import com.skk.jdsbackend.entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCaseEntity(Case caseEntity);

    List<Document> findByCaseEntityOrderByUploadedAtDesc(Case caseEntity);

    long countByCaseEntityId(Long caseId);

    @Query("SELECT d.caseEntity.id, COUNT(d) FROM Document d GROUP BY d.caseEntity.id")
    List<Object[]> countAllGroupedByCaseId();
}
