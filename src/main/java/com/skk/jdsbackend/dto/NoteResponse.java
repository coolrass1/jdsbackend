package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteResponse {

    private Long id;
    private String content;
    private UserSummaryDto author;
    private Long caseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
