package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for ONLYOFFICE Document Server callbacks
 * Error codes:
 * 0 - No errors (success)
 * 1 - Document key error
 * 2 - Callback URL error
 * 3 - Internal server error
 * 4 - Download error
 * 5 - Conversion error
 * 6 - Unknown error
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlyOfficeCallbackResponse {

    /**
     * Error code (0 = success)
     */
    private Integer error;

    /**
     * Optional error message
     */
    private String message;

    public OnlyOfficeCallbackResponse(Integer error) {
        this.error = error;
    }

    public static OnlyOfficeCallbackResponse success() {
        return new OnlyOfficeCallbackResponse(0);
    }

    public static OnlyOfficeCallbackResponse error(Integer errorCode, String message) {
        return new OnlyOfficeCallbackResponse(errorCode, message);
    }
}
