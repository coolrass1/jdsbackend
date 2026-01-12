package com.skk.jdsbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", unique = true)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by_user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User lastModifiedByUser;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus status = CaseStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CasePriority priority = CasePriority.MEDIUM;

    @Column(name = "id_checked", nullable = false)
    private Boolean idChecked = false;

    @Column(name = "id_checked_comment", columnDefinition = "TEXT")
    private String idCheckedComment;

    @Column(name = "due_date", nullable = true)
    private LocalDateTime dueDate;

    // Many-to-One: Case → User (assigned user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    // Many-to-One: Case → Client
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    // One-to-Many: Case → Note
    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    // One-to-Many: Case → Task
    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();
    // One-to-Many: Case → Document
    @OneToMany(mappedBy = "caseEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods to manage bidirectional relationships
    public void addNote(Note note) {
        notes.add(note);
        note.setCaseEntity(this);
    }

    public void removeNote(Note note) {
        notes.remove(note);
        note.setCaseEntity(null);
    }

    public void addDocument(Document document) {
        documents.add(document);
        document.setCaseEntity(this);
    }

    public void removeDocument(Document document) {
        documents.remove(document);
        document.setCaseEntity(null);
    }
}
