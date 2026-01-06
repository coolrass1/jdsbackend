package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatsDto {
    private Long clientId;
    private String clientName;
    private String email;
    private Long totalCases;
    private Long activeCases;
    private Long closedCases;
    private Long totalDocuments;
}
