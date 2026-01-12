package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponse {

    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private Long caseId;
    private String caseName;
    private Long performedById;
    private String performedByUsername;
    private String details;
    private LocalDateTime performedAt;
}
