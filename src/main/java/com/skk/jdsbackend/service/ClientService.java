package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.ClientCreateRequest;
import com.skk.jdsbackend.dto.ClientResponse;
import com.skk.jdsbackend.dto.ClientUpdateRequest;
import com.skk.jdsbackend.entity.Client;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientResponse createClient(ClientCreateRequest request) {
        // Check if email already exists
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Client with email " + request.getEmail() + " already exists");
        }

        Client client = new Client();
        client.setFirstname(request.getFirstname());
        client.setLastname(request.getLastname());
        client.setEmail(request.getEmail());
        client.setNi_number(request.getNi_number());
        client.setPhone(request.getPhone());
        client.setAddress(request.getAddress());
        client.setCompany(request.getCompany());
        client.setOccupation(request.getOccupation());
        client.setAdditionalNote(request.getAdditionalNote());
        client.setHasConflictOfInterest(request.getHasConflictOfInterest() != null ? request.getHasConflictOfInterest() : false);
        client.setConflictOfInterestComment(request.getConflictOfInterestComment());

        Client savedClient = clientRepository.save(client);
        return mapToResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return mapToResponse(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> searchClientsByName(String name) {
        return clientRepository.findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(name, name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientResponse updateClient(Long id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        if (request.getFirstname() != null) {
            client.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            client.setLastname(request.getLastname());
        }
        if (request.getEmail() != null) {
            // Check if new email already exists for a different client
            if (!request.getEmail().equals(client.getEmail()) &&
                    clientRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Client with email " + request.getEmail() + " already exists");
            }
            client.setEmail(request.getEmail());
        }
        if (request.getNi_number() != null) {
            client.setNi_number(request.getNi_number());
        }
        if (request.getPhone() != null) {
            client.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            client.setAddress(request.getAddress());
        }
        if (request.getCompany() != null) {
            client.setCompany(request.getCompany());
        }
        if (request.getOccupation() != null) {
            client.setOccupation(request.getOccupation());
        }
        if (request.getAdditionalNote() != null) {
            client.setAdditionalNote(request.getAdditionalNote());
        }
        if (request.getHasConflictOfInterest() != null) {
            client.setHasConflictOfInterest(request.getHasConflictOfInterest());
        }
        if (request.getConflictOfInterestComment() != null) {
            client.setConflictOfInterestComment(request.getConflictOfInterestComment());
        }

        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // Check if client has associated cases
        if (client.getCases() != null && !client.getCases().isEmpty()) {
            throw new IllegalStateException("Cannot delete client with existing cases. " +
                    "Please reassign or delete the cases first.");
        }

        clientRepository.deleteById(id);
    }

    private ClientResponse mapToResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setFirstname(client.getFirstname());
        response.setLastname(client.getLastname());
        response.setEmail(client.getEmail());
        response.setNi_number(client.getNi_number());
        response.setPhone(client.getPhone());
        response.setAddress(client.getAddress());
        response.setCompany(client.getCompany());
        response.setOccupation(client.getOccupation());
        response.setAdditionalNote(client.getAdditionalNote());
        response.setHasConflictOfInterest(client.getHasConflictOfInterest());
        response.setConflictOfInterestComment(client.getConflictOfInterestComment());
        response.setCasesCount(client.getCases() != null ? client.getCases().size() : 0);
        response.setCreatedAt(client.getCreatedAt());
        response.setUpdatedAt(client.getUpdatedAt());

        return response;
    }
}
