package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.CasePriority;
import com.skk.jdsbackend.entity.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Long assignedUserId;

    private Long clientId;
}
