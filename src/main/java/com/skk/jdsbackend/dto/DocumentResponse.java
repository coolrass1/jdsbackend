package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Long caseId;
    private LocalDateTime uploadedAt;
}
