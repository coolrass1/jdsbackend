package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.MessageResponse;
import com.skk.jdsbackend.dto.TaskCreateRequest;
import com.skk.jdsbackend.dto.TaskResponse;
import com.skk.jdsbackend.dto.TaskUpdateRequest;
import com.skk.jdsbackend.entity.TaskStatus;
import com.skk.jdsbackend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponse>> getTasksByCase(@PathVariable Long caseId) {
        List<TaskResponse> tasks = taskService.getTasksByCase(caseId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponse>> getTasksByAssignedUser(@PathVariable Long userId) {
        List<TaskResponse> tasks = taskService.getTasksByAssignedUser(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        List<TaskResponse> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks() {
        List<TaskResponse> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(new MessageResponse("Task deleted successfully"));
    }
}
