package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.TaskPriority;
import com.skk.jdsbackend.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;

    private Long caseId;
    private String caseTitle;

    private UserSummaryDto assignedUser;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
