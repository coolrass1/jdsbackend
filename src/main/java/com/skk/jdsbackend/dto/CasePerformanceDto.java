package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CasePerformanceDto {
    private Long totalCasesResolved;
    private Double averageResolutionDays;
    private Long fastestResolutionDays;
    private Long slowestResolutionDays;
    private Long casesResolvedThisMonth;
    private Long casesResolvedLastMonth;
}
