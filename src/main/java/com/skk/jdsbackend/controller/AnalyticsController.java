package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.*;
import com.skk.jdsbackend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/dashboard
     * Get overall dashboard statistics
     * Available to all authenticated users
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = analyticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/analytics/cases/by-status
     * Get case count by status
     * Available to all authenticated users
     */
    @GetMapping("/cases/by-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CaseStatsByStatusDto>> getCaseStatsByStatus() {
        List<CaseStatsByStatusDto> stats = analyticsService.getCaseStatsByStatus();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/analytics/cases/by-priority
     * Get case count by priority
     * Available to all authenticated users
     */
    @GetMapping("/cases/by-priority")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CaseStatsByPriorityDto>> getCaseStatsByPriority() {
        List<CaseStatsByPriorityDto> stats = analyticsService.getCaseStatsByPriority();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/analytics/cases/performance
     * Get case performance metrics (resolution times, etc.)
     * Available to ADMIN and MANAGER roles
     */
    @GetMapping("/cases/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CasePerformanceDto> getCasePerformance() {
        CasePerformanceDto performance = analyticsService.getCasePerformance();
        return ResponseEntity.ok(performance);
    }

    /**
     * GET /api/analytics/tasks/by-status
     * Get task count by status
     * Available to all authenticated users
     */
    @GetMapping("/tasks/by-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskStatsDto>> getTaskStatsByStatus() {
        List<TaskStatsDto> stats = analyticsService.getTaskStatsByStatus();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/analytics/users/workload
     * Get workload distribution across users
     * Available to ADMIN and MANAGER roles
     */
    @GetMapping("/users/workload")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<UserWorkloadDto>> getUserWorkload() {
        List<UserWorkloadDto> workload = analyticsService.getUserWorkload();
        return ResponseEntity.ok(workload);
    }

    /**
     * GET /api/analytics/clients/stats
     * Get statistics for all clients
     * Available to ADMIN and MANAGER roles
     */
    @GetMapping("/clients/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ClientStatsDto>> getClientStats() {
        List<ClientStatsDto> stats = analyticsService.getClientStats();
        return ResponseEntity.ok(stats);
    }
}
