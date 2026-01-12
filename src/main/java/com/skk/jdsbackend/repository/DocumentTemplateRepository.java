package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {
    
    List<DocumentTemplate> findByIsActiveTrueOrderByNameAsc();
    
    List<DocumentTemplate> findByCategoryOrderByNameAsc(String category);
    
    List<DocumentTemplate> findByIsActiveTrueAndCategoryOrderByNameAsc(String category);
}
