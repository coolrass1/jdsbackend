package com.skk.jdsbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "case_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Case caseEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseParticipantRole role;

    public CaseParticipant(Case caseEntity, User user, CaseParticipantRole role) {
        this.caseEntity = caseEntity;
        this.user = user;
        this.role = role;
    }
}
