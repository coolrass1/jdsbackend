package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignDocumentRequest {
    private String signatureToken;
    private String signatureData; // Base64 encoded signature image
}
