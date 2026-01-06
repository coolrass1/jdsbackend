package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.Task;
import com.skk.jdsbackend.entity.TaskStatus;
import com.skk.jdsbackend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Override
    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    Optional<Task> findById(Long id);

    @Override
    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    List<Task> findAll();

    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    List<Task> findByCaseEntityId(Long caseId);

    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    List<Task> findByCaseEntity(Case caseEntity);

    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    List<Task> findByAssignedUserId(Long userId);

    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    List<Task> findByAssignedUser(User assignedUser);

    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    List<Task> findByStatus(TaskStatus status);

    @EntityGraph(attributePaths = { "caseEntity", "assignedUser" })
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != 'COMPLETED' AND t.status != 'CANCELLED'")
    List<Task> findOverdueTasks(LocalDate date);
}
