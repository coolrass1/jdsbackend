package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.*;
import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.CaseStatus;
import com.skk.jdsbackend.entity.Client;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.ClientRepository;
import com.skk.jdsbackend.repository.DocumentRepository;
import com.skk.jdsbackend.repository.NoteRepository;
import com.skk.jdsbackend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final NoteRepository noteRepository;
    private final DocumentRepository documentRepository;
    private final ActivityService activityService;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Transactional
    public CaseResponse createCase(CaseCreateRequest request, Long creatorId) {
        Case caseEntity = new Case();
        caseEntity.setTitle(request.getTitle());
        caseEntity.setDescription(request.getDescription());
        caseEntity.setStatus(request.getStatus() != null ? request.getStatus() : CaseStatus.OPEN);
        caseEntity.setPriority(request.getPriority());

        if (request.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getAssignedUserId()));
            caseEntity.setAssignedUser(assignedUser);
        }

        if (request.getClientId() != null) {
            Client client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Client not found with id: " + request.getClientId()));
            caseEntity.setClient(client);
        }

        // Audit fields
        if (request.getReferenceNumber() != null && !request.getReferenceNumber().isBlank()) {
            caseEntity.setReferenceNumber(request.getReferenceNumber());
        } else {
            caseEntity.setReferenceNumber(sequenceGeneratorService.generateNextCaseReference());
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));
        caseEntity.setCreatedByUser(creator);
        caseEntity.setLastModifiedByUser(creator);

        Case savedCase = caseRepository.save(caseEntity);

        // Log activity
        activityService.logActivity(
                "case_created",
                "CASE",
                savedCase.getId(),
                savedCase.getId(),
                String.format("Created case: %s (Priority: %s)", request.getTitle(), request.getPriority()));

        return mapToResponse(savedCase);
    }

    @Transactional(readOnly = true)
    public CaseResponse getCaseById(Long id) {
        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + id));
        return mapToResponse(caseEntity);
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getAllCases() {
        List<Case> cases = caseRepository.findAll();

        // Batch load all counts in 2 queries instead of 2N queries
        Map<Long, Long> noteCounts = noteRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        Map<Long, Long> docCounts = documentRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return cases.stream()
                .map(c -> mapToResponse(c, noteCounts, docCounts))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getCasesByStatus(CaseStatus status) {
        List<Case> cases = caseRepository.findByStatus(status);

        Map<Long, Long> noteCounts = noteRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        Map<Long, Long> docCounts = documentRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return cases.stream()
                .map(c -> mapToResponse(c, noteCounts, docCounts))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getCasesByAssignedUser(Long userId) {
        List<Case> cases = caseRepository.findByAssignedUserId(userId);

        Map<Long, Long> noteCounts = noteRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        Map<Long, Long> docCounts = documentRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return cases.stream()
                .map(c -> mapToResponse(c, noteCounts, docCounts))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> searchCasesByTitle(String title) {
        List<Case> cases = caseRepository.findByTitleContainingIgnoreCase(title);

        Map<Long, Long> noteCounts = noteRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
        Map<Long, Long> docCounts = documentRepository.countAllGroupedByCaseId().stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return cases.stream()
                .map(c -> mapToResponse(c, noteCounts, docCounts))
                .collect(Collectors.toList());
    }

    @Transactional
    public CaseResponse updateCase(Long id, CaseUpdateRequest request, Long modifierId) {
        Case caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + id));

        if (request.getTitle() != null) {
            caseEntity.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            caseEntity.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            caseEntity.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            caseEntity.setPriority(request.getPriority());
        }
        if (request.getIdChecked() != null) {
            caseEntity.setIdChecked(request.getIdChecked());
        }
        if (request.getIdCheckedComment() != null) {
            caseEntity.setIdCheckedComment(request.getIdCheckedComment());
        }
        if (request.getDueDate() != null) {
            caseEntity.setDueDate(request.getDueDate());
        }
        if (request.getAssignedUserId() != null) {
            User assignedUser = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + request.getAssignedUserId()));
            caseEntity.setAssignedUser(assignedUser);
        }
        if (request.getClientId() != null) {
            Client client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Client not found with id: " + request.getClientId()));
            caseEntity.setClient(client);
        }

        if (request.getReferenceNumber() != null) {
            caseEntity.setReferenceNumber(request.getReferenceNumber());
        }

        User modifier = userRepository.findById(modifierId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierId));
        caseEntity.setLastModifiedByUser(modifier);

        Case updatedCase = caseRepository.save(caseEntity);
        return mapToResponse(updatedCase);
    }

    @Transactional
    public void deleteCase(Long id) {
        if (!caseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Case not found with id: " + id);
        }
        caseRepository.deleteById(id);
    }

    private CaseResponse mapToResponse(Case caseEntity) {
        CaseResponse response = new CaseResponse();
        response.setId(caseEntity.getId());
        response.setTitle(caseEntity.getTitle());
        response.setDescription(caseEntity.getDescription());
        response.setStatus(caseEntity.getStatus());
        response.setPriority(caseEntity.getPriority());
        response.setCreatedAt(caseEntity.getCreatedAt());
        response.setUpdatedAt(caseEntity.getUpdatedAt());
        response.setReferenceNumber(caseEntity.getReferenceNumber());
        if (caseEntity.getCreatedByUser() != null) {
            response.setCreatedByUser(mapToUserSummary(caseEntity.getCreatedByUser()));
        }
        if (caseEntity.getLastModifiedByUser() != null) {
            response.setLastModifiedByUser(mapToUserSummary(caseEntity.getLastModifiedByUser()));
        }

        if (caseEntity.getAssignedUser() != null) {
            UserSummaryDto userSummary = new UserSummaryDto();
            userSummary.setId(caseEntity.getAssignedUser().getId());
            userSummary.setUsername(caseEntity.getAssignedUser().getUsername());
            userSummary.setEmail(caseEntity.getAssignedUser().getEmail());
            response.setAssignedUser(userSummary);
        }

        if (caseEntity.getClient() != null) {
            ClientSummaryDto clientSummary = new ClientSummaryDto();
            clientSummary.setId(caseEntity.getClient().getId());
            clientSummary.setFirstname(caseEntity.getClient().getFirstname());
            clientSummary.setLastname(caseEntity.getClient().getLastname());
            clientSummary.setEmail(caseEntity.getClient().getEmail());
            clientSummary.setCompany(caseEntity.getClient().getCompany());
            response.setClient(clientSummary);
        }

        // Use efficient count queries for single entity lookups
        response.setNotesCount((int) noteRepository.countByCaseEntityId(caseEntity.getId()));
        response.setDocumentsCount((int) documentRepository.countByCaseEntityId(caseEntity.getId()));

        return response;
    }

    // Overloaded version for batch operations - uses pre-loaded counts
    private CaseResponse mapToResponse(Case caseEntity, Map<Long, Long> noteCounts, Map<Long, Long> docCounts) {
        CaseResponse response = new CaseResponse();
        response.setId(caseEntity.getId());
        response.setTitle(caseEntity.getTitle());
        response.setDescription(caseEntity.getDescription());
        response.setStatus(caseEntity.getStatus());
        response.setPriority(caseEntity.getPriority());
        response.setIdChecked(caseEntity.getIdChecked());
        response.setIdCheckedComment(caseEntity.getIdCheckedComment());
        response.setDueDate(caseEntity.getDueDate());
        response.setCreatedAt(caseEntity.getCreatedAt());
        response.setUpdatedAt(caseEntity.getUpdatedAt());
        response.setReferenceNumber(caseEntity.getReferenceNumber());
        if (caseEntity.getCreatedByUser() != null) {
            response.setCreatedByUser(mapToUserSummary(caseEntity.getCreatedByUser()));
        }
        if (caseEntity.getLastModifiedByUser() != null) {
            response.setLastModifiedByUser(mapToUserSummary(caseEntity.getLastModifiedByUser()));
        }

        if (caseEntity.getAssignedUser() != null) {
            UserSummaryDto userSummary = new UserSummaryDto();
            userSummary.setId(caseEntity.getAssignedUser().getId());
            userSummary.setUsername(caseEntity.getAssignedUser().getUsername());
            userSummary.setEmail(caseEntity.getAssignedUser().getEmail());
            response.setAssignedUser(userSummary);
        }

        if (caseEntity.getClient() != null) {
            ClientSummaryDto clientSummary = new ClientSummaryDto();
            clientSummary.setId(caseEntity.getClient().getId());
            clientSummary.setFirstname(caseEntity.getClient().getFirstname());
            clientSummary.setLastname(caseEntity.getClient().getLastname());
            clientSummary.setEmail(caseEntity.getClient().getEmail());
            clientSummary.setCompany(caseEntity.getClient().getCompany());
            response.setClient(clientSummary);
        }

        // Use pre-loaded counts from batch query
        response.setNotesCount(noteCounts.getOrDefault(caseEntity.getId(), 0L).intValue());
        response.setDocumentsCount(docCounts.getOrDefault(caseEntity.getId(), 0L).intValue());

        return response;
    }

    private UserSummaryDto mapToUserSummary(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getEmail());
    }
}
