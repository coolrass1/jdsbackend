package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.TaskPriority;
import com.skk.jdsbackend.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Assigned User ID is required")
    private Long assignedUserId;
}
