package com.skk.jdsbackend.entity;

public enum Permission {
    // Case permissions
    CASE_READ,
    CASE_WRITE,
    CASE_DELETE,
    
    // Client permissions
    CLIENT_READ,
    CLIENT_WRITE,
    CLIENT_DELETE,
    
    // Document permissions
    DOCUMENT_READ,
    DOCUMENT_WRITE,
    DOCUMENT_DELETE,
    DOCUMENT_SIGN,
    
    // User management
    USER_READ,
    USER_WRITE,
    USER_DELETE,
    
    // Task permissions
    TASK_READ,
    TASK_WRITE,
    TASK_DELETE,
    TASK_ASSIGN,
    
    // Note permissions
    NOTE_READ,
    NOTE_WRITE,
    NOTE_DELETE,
    
    // Analytics
    ANALYTICS_VIEW,
    
    // System administration
    SYSTEM_ADMIN
}
