package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWorkloadDto {
    private Long userId;
    private String username;
    private String fullName;
    private Long assignedCases;
    private Long activeCases;
    private Long assignedTasks;
    private Long completedTasks;
    private Long overdueTasks;
}
