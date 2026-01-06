package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseStatsByStatusDto {
    private CaseStatus status;
    private Long count;
}
