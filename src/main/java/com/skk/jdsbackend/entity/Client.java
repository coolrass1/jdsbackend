package com.skk.jdsbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String firstname;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String lastname;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String ni_number;

    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Size(max = 100)
    @Column(length = 100)
    private String company;

    @Size(max = 100)
    @Column(length = 100)
    private String occupation;

    @Column(name = "additional_note", columnDefinition = "TEXT")
    private String additionalNote;

    @Column(name = "has_conflict_of_interest")
    private Boolean hasConflictOfInterest = false;

    @Column(name = "conflict_of_interest_comment", columnDefinition = "TEXT")
    private String conflictOfInterestComment;

    // One-to-Many: Client → Case
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Case> cases = new ArrayList<>();

    // Many-to-Many: Client ↔ User (assigned users)
    @ManyToMany(mappedBy = "clients", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> assignedUsers = new HashSet<>();

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

    // Helper methods to manage bidirectional relationships
    public void addCase(Case caseEntity) {
        cases.add(caseEntity);
        caseEntity.setClient(this);
    }

    public void removeCase(Case caseEntity) {
        cases.remove(caseEntity);
        caseEntity.setClient(null);
    }

    public void addUser(User user) {
        this.assignedUsers.add(user);
        user.getClients().add(this);
    }

    public void removeUser(User user) {
        this.assignedUsers.remove(user);
        user.getClients().remove(this);
    }
}
