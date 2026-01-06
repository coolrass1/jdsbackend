package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.CasePriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseStatsByPriorityDto {
    private CasePriority priority;
    private Long count;
}
