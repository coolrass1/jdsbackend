package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionResponse {
    private Long id;
    private Long documentId;
    private Integer versionNumber;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadedByUsername;
    private String changeDescription;
    private LocalDateTime createdAt;
}
