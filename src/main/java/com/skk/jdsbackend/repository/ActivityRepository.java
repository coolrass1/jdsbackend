package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    List<Activity> findByRelatedCaseIdOrderByPerformedAtDesc(Long caseId);
    
    List<Activity> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Long entityId);
    
    List<Activity> findByPerformedByIdOrderByPerformedAtDesc(Long userId);
    
    List<Activity> findAllByOrderByPerformedAtDesc();
}
