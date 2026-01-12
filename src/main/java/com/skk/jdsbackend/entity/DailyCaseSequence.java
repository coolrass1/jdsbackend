package com.skk.jdsbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_case_sequences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCaseSequence {

    @Id
    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Long sequenceValue;
}
