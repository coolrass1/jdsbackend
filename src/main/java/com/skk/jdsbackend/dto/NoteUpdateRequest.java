package com.skk.jdsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteUpdateRequest {

    @NotBlank(message = "Content is required")
    private String content;
}
