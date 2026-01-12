package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.ActivityResponse;
import com.skk.jdsbackend.entity.Activity;
import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.repository.ActivityRepository;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.UserRepository;
import com.skk.jdsbackend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;

    @Transactional
    public void logActivity(String action, String entityType, Long entityId, Long caseId, String details) {
        Activity activity = new Activity();
        activity.setAction(action);
        activity.setEntityType(entityType);
        activity.setEntityId(entityId);
        activity.setDetails(details);

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElse(null);
            activity.setPerformedBy(user);
        }

        // Associate with case if provided
        if (caseId != null) {
            Case relatedCase = caseRepository.findById(caseId).orElse(null);
            activity.setRelatedCase(relatedCase);
        }

        activityRepository.save(activity);
    }

    public List<ActivityResponse> getActivitiesByCaseId(Long caseId) {
        List<Activity> activities = activityRepository.findByRelatedCaseIdOrderByPerformedAtDesc(caseId);
        return activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityResponse> getActivitiesByEntity(String entityType, Long entityId) {
        List<Activity> activities = activityRepository.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(entityType, entityId);
        return activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityResponse> getActivitiesByUser(Long userId) {
        List<Activity> activities = activityRepository.findByPerformedByIdOrderByPerformedAtDesc(userId);
        return activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityResponse> getAllActivities() {
        List<Activity> activities = activityRepository.findAllByOrderByPerformedAtDesc();
        return activities.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ActivityResponse mapToResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setAction(activity.getAction());
        response.setEntityType(activity.getEntityType());
        response.setEntityId(activity.getEntityId());
        response.setDetails(activity.getDetails());
        response.setPerformedAt(activity.getPerformedAt());

        if (activity.getRelatedCase() != null) {
            response.setCaseId(activity.getRelatedCase().getId());
            response.setCaseName(activity.getRelatedCase().getTitle());
        }

        if (activity.getPerformedBy() != null) {
            response.setPerformedById(activity.getPerformedBy().getId());
            response.setPerformedByUsername(activity.getPerformedBy().getUsername());
        }

        return response;
    }
}
