package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.CaseStatus;
import com.skk.jdsbackend.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    @Override
    @EntityGraph(attributePaths = { "assignedUser", "client" })
    Optional<Case> findById(Long id);

    @Override
    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findAll();

    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findByAssignedUserId(Long userId);

    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findByAssignedUser(User user);

    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findByStatus(CaseStatus status);

    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findByAssignedUserAndStatus(User user, CaseStatus status);

    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findByClient(com.skk.jdsbackend.entity.Client client);

    @EntityGraph(attributePaths = { "assignedUser", "client" })
    List<Case> findByClientId(Long clientId);
}
