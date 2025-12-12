package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.*;
import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.CaseStatus;
import com.skk.jdsbackend.entity.Client;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.ClientRepository;
import com.skk.jdsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public CaseResponse createCase(CaseCreateRequest request) {
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

        Case savedCase = caseRepository.save(caseEntity);
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
        return caseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getCasesByStatus(CaseStatus status) {
        return caseRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getCasesByAssignedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return caseRepository.findByAssignedUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> searchCasesByTitle(String title) {
        return caseRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CaseResponse updateCase(Long id, CaseUpdateRequest request) {
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

        response.setNotesCount(caseEntity.getNotes() != null ? caseEntity.getNotes().size() : 0);
        response.setDocumentsCount(caseEntity.getDocuments() != null ? caseEntity.getDocuments().size() : 0);

        return response;
    }
}
