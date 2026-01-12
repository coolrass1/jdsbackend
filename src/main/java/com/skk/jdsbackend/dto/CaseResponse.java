package com.skk.jdsbackend.dto;

import com.skk.jdsbackend.entity.CasePriority;
import com.skk.jdsbackend.entity.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseResponse {

    private Long id;
    private String title;
    private String description;
    private CaseStatus status;
    private CasePriority priority;
    private Boolean idChecked;
    private String idCheckedComment;
    private LocalDateTime dueDate;
    private UserSummaryDto assignedUser;
    private ClientSummaryDto client;
    private Integer notesCount;
    private Integer documentsCount;
    private String referenceNumber;
    private UserSummaryDto createdByUser;
    private UserSummaryDto lastModifiedByUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
