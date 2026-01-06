package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.*;
import com.skk.jdsbackend.entity.*;
import com.skk.jdsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final CaseRepository caseRepository;
    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    /**
     * Get overall dashboard statistics
     */
    public DashboardStatsDto getDashboardStats() {
        List<Case> allCases = caseRepository.findAll();
        List<Task> allTasks = taskRepository.findAll();
        
        long totalCases = allCases.size();
        long activeCases = allCases.stream()
                .filter(c -> c.getStatus() != CaseStatus.CLOSED && c.getStatus() != CaseStatus.RESOLVED)
                .count();
        long closedCases = allCases.stream()
                .filter(c -> c.getStatus() == CaseStatus.CLOSED || c.getStatus() == CaseStatus.RESOLVED)
                .count();
        
        long totalTasks = allTasks.size();
        long overdueTasks = taskRepository.findOverdueTasks(LocalDate.now()).size();
        long completedTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .count();
        
        long totalClients = clientRepository.count();
        long totalDocuments = documentRepository.count();
        
        // Calculate average resolution time for closed/resolved cases
        Double averageResolutionDays = calculateAverageResolutionDays(allCases);
        
        return DashboardStatsDto.builder()
                .totalCases(totalCases)
                .activeCases(activeCases)
                .closedCases(closedCases)
                .totalTasks(totalTasks)
                .overdueTasks(overdueTasks)
                .completedTasks(completedTasks)
                .totalClients(totalClients)
                .totalDocuments(totalDocuments)
                .averageCaseResolutionDays(averageResolutionDays)
                .build();
    }

    /**
     * Get case statistics by status
     */
    public List<CaseStatsByStatusDto> getCaseStatsByStatus() {
        List<Case> allCases = caseRepository.findAll();
        
        Map<CaseStatus, Long> statusCounts = allCases.stream()
                .collect(Collectors.groupingBy(Case::getStatus, Collectors.counting()));
        
        return statusCounts.entrySet().stream()
                .map(entry -> CaseStatsByStatusDto.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(dto -> dto.getStatus().name()))
                .collect(Collectors.toList());
    }

    /**
     * Get case statistics by priority
     */
    public List<CaseStatsByPriorityDto> getCaseStatsByPriority() {
        List<Case> allCases = caseRepository.findAll();
        
        Map<CasePriority, Long> priorityCounts = allCases.stream()
                .collect(Collectors.groupingBy(Case::getPriority, Collectors.counting()));
        
        return priorityCounts.entrySet().stream()
                .map(entry -> CaseStatsByPriorityDto.builder()
                        .priority(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(dto -> dto.getPriority().name()))
                .collect(Collectors.toList());
    }

    /**
     * Get task statistics by status
     */
    public List<TaskStatsDto> getTaskStatsByStatus() {
        List<Task> allTasks = taskRepository.findAll();
        
        Map<TaskStatus, Long> statusCounts = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        
        return statusCounts.entrySet().stream()
                .map(entry -> TaskStatsDto.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(dto -> dto.getStatus().name()))
                .collect(Collectors.toList());
    }

    /**
     * Get user workload distribution
     */
    public List<UserWorkloadDto> getUserWorkload() {
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
                .map(user -> {
                    List<Case> userCases = caseRepository.findByAssignedUser(user);
                    List<Task> userTasks = taskRepository.findByAssignedUser(user);
                    
                    long assignedCases = userCases.size();
                    long activeCases = userCases.stream()
                            .filter(c -> c.getStatus() != CaseStatus.CLOSED && c.getStatus() != CaseStatus.RESOLVED)
                            .count();
                    
                    long assignedTasks = userTasks.size();
                    long completedTasks = userTasks.stream()
                            .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                            .count();
                    long overdueTasks = userTasks.stream()
                            .filter(t -> t.getDueDate() != null && 
                                       t.getDueDate().isBefore(LocalDate.now()) &&
                                       t.getStatus() != TaskStatus.COMPLETED &&
                                       t.getStatus() != TaskStatus.CANCELLED)
                            .count();
                    
                    return UserWorkloadDto.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .fullName(user.getUsername())
                            .assignedCases(assignedCases)
                            .activeCases(activeCases)
                            .assignedTasks(assignedTasks)
                            .completedTasks(completedTasks)
                            .overdueTasks(overdueTasks)
                            .build();
                })
                .filter(dto -> dto.getAssignedCases() > 0 || dto.getAssignedTasks() > 0)
                .sorted(Comparator.comparing(UserWorkloadDto::getActiveCases).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get case performance metrics
     */
    public CasePerformanceDto getCasePerformance() {
        List<Case> allCases = caseRepository.findAll();
        
        List<Case> resolvedCases = allCases.stream()
                .filter(c -> c.getStatus() == CaseStatus.RESOLVED || c.getStatus() == CaseStatus.CLOSED)
                .collect(Collectors.toList());
        
        long totalResolved = resolvedCases.size();
        Double averageResolution = calculateAverageResolutionDays(resolvedCases);
        
        // Calculate fastest and slowest resolution
        Long fastest = null;
        Long slowest = null;
        
        if (!resolvedCases.isEmpty()) {
            List<Long> resolutionDays = resolvedCases.stream()
                    .map(c -> ChronoUnit.DAYS.between(c.getCreatedAt(), c.getUpdatedAt()))
                    .sorted()
                    .collect(Collectors.toList());
            
            if (!resolutionDays.isEmpty()) {
                fastest = resolutionDays.get(0);
                slowest = resolutionDays.get(resolutionDays.size() - 1);
            }
        }
        
        // Cases resolved this month and last month
        LocalDateTime startOfThisMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        
        long casesThisMonth = resolvedCases.stream()
                .filter(c -> c.getUpdatedAt().isAfter(startOfThisMonth))
                .count();
        
        long casesLastMonth = resolvedCases.stream()
                .filter(c -> c.getUpdatedAt().isAfter(startOfLastMonth) && 
                           c.getUpdatedAt().isBefore(startOfThisMonth))
                .count();
        
        return CasePerformanceDto.builder()
                .totalCasesResolved(totalResolved)
                .averageResolutionDays(averageResolution)
                .fastestResolutionDays(fastest)
                .slowestResolutionDays(slowest)
                .casesResolvedThisMonth(casesThisMonth)
                .casesResolvedLastMonth(casesLastMonth)
                .build();
    }

    /**
     * Get client statistics
     */
    public List<ClientStatsDto> getClientStats() {
        List<Client> allClients = clientRepository.findAll();
        
        return allClients.stream()
                .map(client -> {
                    List<Case> clientCases = caseRepository.findByClient(client);
                    
                    long totalCases = clientCases.size();
                    long activeCases = clientCases.stream()
                            .filter(c -> c.getStatus() != CaseStatus.CLOSED && c.getStatus() != CaseStatus.RESOLVED)
                            .count();
                    long closedCases = clientCases.stream()
                            .filter(c -> c.getStatus() == CaseStatus.CLOSED || c.getStatus() == CaseStatus.RESOLVED)
                            .count();
                    
                    long totalDocuments = clientCases.stream()
                            .mapToLong(c -> c.getDocuments().size())
                            .sum();
                    
                    return ClientStatsDto.builder()
                            .clientId(client.getId())
                            .clientName(client.getFirstname() + " " + client.getLastname())
                            .email(client.getEmail())
                            .totalCases(totalCases)
                            .activeCases(activeCases)
                            .closedCases(closedCases)
                            .totalDocuments(totalDocuments)
                            .build();
                })
                .sorted(Comparator.comparing(ClientStatsDto::getTotalCases).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Helper method to calculate average resolution days
     */
    private Double calculateAverageResolutionDays(List<Case> cases) {
        List<Case> resolvedCases = cases.stream()
                .filter(c -> c.getStatus() == CaseStatus.RESOLVED || c.getStatus() == CaseStatus.CLOSED)
                .collect(Collectors.toList());
        
        if (resolvedCases.isEmpty()) {
            return 0.0;
        }
        
        double totalDays = resolvedCases.stream()
                .mapToLong(c -> ChronoUnit.DAYS.between(c.getCreatedAt(), c.getUpdatedAt()))
                .sum();
        
        return totalDays / resolvedCases.size();
    }
}
