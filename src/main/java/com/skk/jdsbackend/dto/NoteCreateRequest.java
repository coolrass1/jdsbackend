package com.skk.jdsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteCreateRequest {

    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Case ID is required")
    private Long caseId;
}
