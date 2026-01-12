package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.DailyClientSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyClientSequenceRepository extends JpaRepository<DailyClientSequence, LocalDate> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DailyClientSequence> findByDate(LocalDate date);
}
