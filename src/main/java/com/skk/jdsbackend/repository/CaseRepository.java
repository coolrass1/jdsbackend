package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.CaseStatus;
import com.skk.jdsbackend.entity.User;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

        @Override
        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        Optional<Case> findById(Long id);

        @Override
        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findAll();

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findByAssignedUserId(Long userId);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findByAssignedUser(User user);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findByStatus(CaseStatus status);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findByAssignedUserAndStatus(User user, CaseStatus status);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findByTitleContainingIgnoreCase(String title);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findByClient(com.skk.jdsbackend.entity.Client client);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        List<Case> findByClientId(Long clientId);

        @Query("SELECT u FROM Case u WHERE u.deleted = true")
        List<Case> findDeleted();

        @Query(value = "SELECT * FROM cases WHERE id = ?1", nativeQuery = true)
        Optional<Case> findByIdIncludingDeleted(Long id);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        @Query("SELECT c FROM Case c JOIN c.participants p WHERE p.user.id = :userId")
        List<Case> findByParticipantId(Long userId);

        @EntityGraph(attributePaths = { "assignedUser", "client", "participants" })
        @Query("SELECT DISTINCT c FROM Case c LEFT JOIN c.participants p WHERE c.assignedUser.id = :userId OR p.user.id = :userId")
        List<Case> findAllRelatedToUser(@Param("userId") Long userId);
}
