package com.skk.jdsbackend.service;

import com.skk.jdsbackend.entity.DailyClientSequence;
import com.skk.jdsbackend.entity.DailyCaseSequence;
import com.skk.jdsbackend.repository.DailyClientSequenceRepository;
import com.skk.jdsbackend.repository.DailyCaseSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final DailyClientSequenceRepository sequenceRepository;
    private final DailyCaseSequenceRepository caseSequenceRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextClientReference() {
        LocalDate today = LocalDate.now();

        DailyClientSequence sequence = sequenceRepository.findByDate(today)
                .orElseGet(() -> new DailyClientSequence(today, 0L));

        sequence.setSequenceValue(sequence.getSequenceValue() + 1);
        sequenceRepository.save(sequence);

        // Format: YYYY-MM-DD-0001
        return String.format("%s-%04d", today.format(DATE_FORMATTER), sequence.getSequenceValue());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextCaseReference() {
        LocalDate today = LocalDate.now();

        DailyCaseSequence sequence = caseSequenceRepository.findByDate(today)
                .orElseGet(() -> new DailyCaseSequence(today, 0L));

        sequence.setSequenceValue(sequence.getSequenceValue() + 1);
        caseSequenceRepository.save(sequence);

        // Format: YYYY-MM-DD-0001
        return String.format("%s-%04d", today.format(DATE_FORMATTER), sequence.getSequenceValue());
    }
}
