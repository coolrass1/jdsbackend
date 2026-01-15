package com.skk.jdsbackend.security;

import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.CaseParticipant;
import com.skk.jdsbackend.entity.CaseParticipantRole;
import com.skk.jdsbackend.entity.Role;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("caseSecurity")
@RequiredArgsConstructor
public class CaseSecurity {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean canAccess(Authentication authentication, Long caseId, String accessType) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);

        if (user == null) {
            return false;
        }

        // Global SUPERVISORS have access to everything
        if (user.getRoles().contains(Role.SUPERVISOR)) {
            return true;
        }

        // Global VIEWERS have read-only access to everything
        if (user.getRoles().contains(Role.VIEWER)) {
            return "READ".equalsIgnoreCase(accessType);
        }

        Case caseEntity = caseRepository.findById(caseId).orElse(null);
        if (caseEntity == null) {
            return true; // Let 404 handle it in controller
        }

        // Owner (CreatedBy or AssignedTo) has full access
        if (isOwnerOrAssigned(caseEntity, user)) {
            return true;
        }

        // Check participation
        return checkParticipation(caseEntity, user, accessType);
    }

    private boolean isOwnerOrAssigned(Case caseEntity, User user) {
        boolean isCreator = caseEntity.getCreatedByUser() != null
                && caseEntity.getCreatedByUser().getId().equals(user.getId());
        boolean isAssigned = caseEntity.getAssignedUser() != null
                && caseEntity.getAssignedUser().getId().equals(user.getId());
        return isCreator || isAssigned;
    }

    private boolean checkParticipation(Case caseEntity, User user, String accessType) {
        for (CaseParticipant participant : caseEntity.getParticipants()) {
            if (participant.getUser().getId().equals(user.getId())) {
                if ("READ".equalsIgnoreCase(accessType)) {
                    return true;
                } else if ("WRITE".equalsIgnoreCase(accessType)) {
                    return participant.getRole() == CaseParticipantRole.EDITOR;
                }
            }
        }
        return false;
    }
}
