package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.CaseResponse;
import com.skk.jdsbackend.dto.ClientCreateRequest;
import com.skk.jdsbackend.dto.ClientResponse;
import com.skk.jdsbackend.dto.ClientUpdateRequest;
import com.skk.jdsbackend.dto.MessageResponse;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.service.CaseService;
import com.skk.jdsbackend.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ClientController {

    private final ClientService clientService;
    private final CaseRepository caseRepository;
    private final CaseService caseService;

    @PostMapping
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientCreateRequest request) {
        ClientResponse response = clientService.createClient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable Long id) {
        ClientResponse response = clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<ClientResponse>> searchClientsByName(@RequestParam String name) {
        List<ClientResponse> clients = clientService.searchClientsByName(name);
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}/cases")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CaseResponse>> getCasesByClient(@PathVariable Long id) {
        // Verify client exists
        clientService.getClientById(id);

        // Get all cases for this client
        List<CaseResponse> cases = caseRepository.findByClientId(id).stream()
                .map(caseEntity -> caseService.getCaseById(caseEntity.getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(cases);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequest request) {
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(new MessageResponse("Client deleted successfully"));
    }
}
