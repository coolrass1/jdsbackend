package com.skk.jdsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for receiving callback requests from ONLYOFFICE Document Server
 * 
 * Status codes:
 * 0 - Document not found
 * 1 - Document being edited
 * 2 - Document ready for saving
 * 3 - Document saving error
 * 4 - Document closed with no changes
 * 6 - Document being edited, but current user is not editing
 * 7 - Error force saving
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlyOfficeCallbackRequest {

    /**
     * Status of the document (2 = ready to save)
     */
    private Integer status;

    /**
     * URL to download the saved document
     */
    private String url;

    /**
     * Document key used by ONLYOFFICE
     */
    private String key;

    /**
     * List of user IDs who edited the document
     */
    private List<String> users;

    /**
     * Actions performed on the document
     */
    private List<Action> actions;

    /**
     * Force save type (if applicable)
     */
    private Integer forcesavetype;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {
        private Integer type;
        private String userid;
    }
}
