package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalCases;
    private Long activeCases;
    private Long closedCases;
    private Long totalTasks;
    private Long overdueTasks;
    private Long completedTasks;
    private Long totalClients;
    private Long totalDocuments;
    private Double averageCaseResolutionDays;
}
