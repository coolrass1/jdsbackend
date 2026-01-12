package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.*;
import com.skk.jdsbackend.entity.*;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.TaskRepository;
import com.skk.jdsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        Case caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + request.getCaseId()));

        User assignedUser = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getAssignedUserId()));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setPriority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM);
        task.setDueDate(request.getDueDate());
        task.setCaseEntity(caseEntity);
        task.setAssignedUser(assignedUser);

        Task savedTask = taskRepository.save(task);
        
        // Log activity
        activityService.logActivity(
            "task_created",
            "TASK",
            savedTask.getId(),
            request.getCaseId(),
            String.format("Created task: %s (Priority: %s, Due: %s)", 
                request.getTitle(), 
                task.getPriority(), 
                task.getDueDate() != null ? task.getDueDate().toString() : "No due date")
        );
        
        return mapToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return mapToResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByCase(Long caseId) {
        return taskRepository.findByCaseEntityId(caseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByAssignedUser(Long userId) {
        return taskRepository.findByAssignedUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getAssignedUserId()));
            task.setAssignedUser(assignedUser);
        }

        Task updatedTask = taskRepository.save(task);
        
        // Log activity
        activityService.logActivity(
            "task_updated",
            "TASK",
            id,
            task.getCaseEntity().getId(),
            String.format("Updated task: %s (Status: %s)", task.getTitle(), task.getStatus())
        );
        
        return mapToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        String taskTitle = task.getTitle();
        Long caseId = task.getCaseEntity() != null ? task.getCaseEntity().getId() : null;
        
        taskRepository.deleteById(id);
        
        // Log activity
        activityService.logActivity(
            "task_deleted",
            "TASK",
            id,
            caseId,
            String.format("Deleted task: %s", taskTitle)
        );
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCaseId(task.getCaseEntity().getId());
        response.setCaseTitle(task.getCaseEntity().getTitle());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        if (task.getAssignedUser() != null) {
            UserSummaryDto userSummary = new UserSummaryDto();
            userSummary.setId(task.getAssignedUser().getId());
            userSummary.setUsername(task.getAssignedUser().getUsername());
            userSummary.setEmail(task.getAssignedUser().getEmail());
            response.setAssignedUser(userSummary);
        }

        return response;
    }
}
