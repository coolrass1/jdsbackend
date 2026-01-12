package com.skk.jdsbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g., "document_deleted", "task_created", "case_updated"

    @Column(nullable = false)
    private String entityType; // e.g., "DOCUMENT", "TASK", "CASE", "CLIENT"

    @Column(nullable = false)
    private Long entityId; // ID of the entity (document ID, task ID, etc.)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case relatedCase; // The case this activity is related to

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User performedBy; // User who performed the action

    @Column(columnDefinition = "TEXT")
    private String details; // JSON or text with additional details (e.g., filename, old/new values)

    @Column(nullable = false)
    private LocalDateTime performedAt;

    @PrePersist
    protected void onCreate() {
        performedAt = LocalDateTime.now();
    }
}
