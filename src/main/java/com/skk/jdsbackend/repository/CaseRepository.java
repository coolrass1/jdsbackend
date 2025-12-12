package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.CaseStatus;
import com.skk.jdsbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    List<Case> findByAssignedUser(User user);

    List<Case> findByStatus(CaseStatus status);

    List<Case> findByAssignedUserAndStatus(User user, CaseStatus status);

    List<Case> findByTitleContainingIgnoreCase(String title);

    List<Case> findByClient(com.skk.jdsbackend.entity.Client client);

    List<Case> findByClientId(Long clientId);
}
