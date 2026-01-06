package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatsDto {
    private TaskStatus status;
    private Long count;
}
