package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.ActivityResponse;
import com.skk.jdsbackend.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAuthority('CASE_READ')")
    public ResponseEntity<List<ActivityResponse>> getActivitiesByCaseId(@PathVariable Long caseId) {
        List<ActivityResponse> activities = activityService.getActivitiesByCaseId(caseId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('CASE_READ')")
    public ResponseEntity<List<ActivityResponse>> getActivitiesByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<ActivityResponse> activities = activityService.getActivitiesByEntity(entityType, entityId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<List<ActivityResponse>> getActivitiesByUser(@PathVariable Long userId) {
        List<ActivityResponse> activities = activityService.getActivitiesByUser(userId);
        return ResponseEntity.ok(activities);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ANALYTICS_VIEW')")
    public ResponseEntity<List<ActivityResponse>> getAllActivities() {
        List<ActivityResponse> activities = activityService.getAllActivities();
        return ResponseEntity.ok(activities);
    }
}
