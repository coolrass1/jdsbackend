package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.CasePriority;
import com.skk.jdsbackend.entity.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    private String description;

    private CaseStatus status;

    private CasePriority priority;

    @NotNull(message = "ID checked status is required")
    private Boolean idChecked = false;

    private String idCheckedComment;

    private LocalDateTime dueDate;

    private Long assignedUserId;

    private Long clientId;

    private String referenceNumber;
}
