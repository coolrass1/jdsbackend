package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.Task;
import com.skk.jdsbackend.entity.TaskStatus;
import com.skk.jdsbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCaseEntity(Case caseEntity);

    List<Task> findByAssignedUser(User assignedUser);

    List<Task> findByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != 'COMPLETED' AND t.status != 'CANCELLED'")
    List<Task> findOverdueTasks(LocalDate date);
}
