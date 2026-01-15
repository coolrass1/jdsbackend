package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.*;
import com.skk.jdsbackend.entity.CaseStatus;
import com.skk.jdsbackend.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.skk.jdsbackend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    @PreAuthorize("hasAuthority('CASE_WRITE')")
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CaseCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CaseResponse response = caseService.createCase(request, userDetails.getId());
        System.out.println("Case created successfully: " + response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@caseSecurity.canAccess(authentication, #id, 'READ')")
    public ResponseEntity<CaseResponse> getCaseById(@PathVariable Long id) {
        CaseResponse response = caseService.getCaseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CASE_READ')")
    public ResponseEntity<List<CaseResponse>> getAllCases() {
        List<CaseResponse> cases = caseService.getAllCases();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CaseResponse>> getCasesByStatus(@PathVariable CaseStatus status) {
        List<CaseResponse> cases = caseService.getCasesByStatus(status);
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CaseResponse>> getCasesByAssignedUser(@PathVariable Long userId) {
        List<CaseResponse> cases = caseService.getCasesByAssignedUser(userId);
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CaseResponse>> searchCasesByTitle(@RequestParam String title) {
        List<CaseResponse> cases = caseService.searchCasesByTitle(title);
        return ResponseEntity.ok(cases);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@caseSecurity.canAccess(authentication, #id, 'WRITE')")
    public ResponseEntity<CaseResponse> updateCase(
            @PathVariable Long id,
            @Valid @RequestBody CaseUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        CaseResponse response = caseService.updateCase(id, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCase(@PathVariable Long id) {
        caseService.deleteCase(id);
        return ResponseEntity.ok(new MessageResponse("Case deleted successfully"));
    }

    @GetMapping("/participant/{userId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CaseResponse>> getCasesByParticipant(@PathVariable Long userId) {
        List<CaseResponse> cases = caseService.getCasesByParticipant(userId);
        return ResponseEntity.ok(cases);
    }

    @PostMapping("/{caseId}/participants")
    @PreAuthorize("@caseSecurity.canAccess(authentication, #caseId, 'WRITE')")
    public ResponseEntity<CaseResponse> addParticipant(
            @PathVariable Long caseId,
            @Valid @RequestBody AddParticipantRequest request) {
        CaseResponse response = caseService.addParticipant(caseId, request.getUserId(), request.getRole());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{caseId}/participants/{userId}")
    @PreAuthorize("@caseSecurity.canAccess(authentication, #caseId, 'WRITE')")
    public ResponseEntity<CaseResponse> removeParticipant(@PathVariable Long caseId, @PathVariable Long userId) {
        return ResponseEntity.ok(caseService.removeParticipant(caseId, userId));
    }

    @GetMapping("/my-cases")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CaseResponse>> getMyCases(
            @AuthenticationPrincipal com.skk.jdsbackend.security.UserDetailsImpl userDetails) {
        return ResponseEntity.ok(caseService.getMyCases(userDetails.getId()));
    }
}
