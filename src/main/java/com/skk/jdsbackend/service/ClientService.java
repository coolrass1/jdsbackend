package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.ClientCreateRequest;
import com.skk.jdsbackend.dto.ClientResponse;
import com.skk.jdsbackend.dto.ClientUpdateRequest;
import com.skk.jdsbackend.dto.UserSummaryDto;
import com.skk.jdsbackend.entity.Client;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.ClientRepository;
import com.skk.jdsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Transactional
    public ClientResponse createClient(ClientCreateRequest request, Long creatorId) {
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
        client.setHasConflictOfInterest(
                request.getHasConflictOfInterest() != null ? request.getHasConflictOfInterest() : false);
        client.setConflictOfInterestComment(request.getConflictOfInterestComment());

        // Generate reference number if not provided
        if (request.getReferenceNumber() != null && !request.getReferenceNumber().isBlank()) {
            client.setReferenceNumber(request.getReferenceNumber());
        } else {
            client.setReferenceNumber(sequenceGeneratorService.generateNextClientReference());
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));
        client.setCreatedByUser(creator);
        // Initially, created by and modified by are the same
        client.setLastModifiedByUser(creator);

        Client savedClient = clientRepository.save(client);

        // Handle assigned users if provided
        if (request.getAssignedUserIds() != null && !request.getAssignedUserIds().isEmpty()) {
            for (Long userId : request.getAssignedUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

                // Use helper method to maintain bidirectional relationship
                // client.addUser(user) calls user.getClients().add(client)
                savedClient.addUser(user);

                // Since User is the owner of the relationship, we must save the User to persist
                // the link
                userRepository.save(user);
            }
        }

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
    public ClientResponse updateClient(Long id, ClientUpdateRequest request, Long modifierId) {
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
        if (request.getReferenceNumber() != null) {
            client.setReferenceNumber(request.getReferenceNumber());
        }

        User modifier = userRepository.findById(modifierId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierId));
        client.setLastModifiedByUser(modifier);

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

    @Transactional
    public ClientResponse assignUsers(Long clientId, List<Long> userIds) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        for (Long userId : userIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
            client.addUser(user);
        }

        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }

    @Transactional
    public ClientResponse removeUser(Long clientId, Long userId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        client.removeUser(user);

        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAssignedUsers(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        return client.getAssignedUsers().stream()
                .map(this::mapToUserSummary)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getClientsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getClients().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
        response.setAssignedUsers(client.getAssignedUsers().stream()
                .map(this::mapToUserSummary)
                .collect(Collectors.toList()));
        response.setReferenceNumber(client.getReferenceNumber());
        if (client.getCreatedByUser() != null) {
            response.setCreatedByUser(mapToUserSummary(client.getCreatedByUser()));
        }
        if (client.getLastModifiedByUser() != null) {
            response.setLastModifiedByUser(mapToUserSummary(client.getLastModifiedByUser()));
        }
        response.setCreatedAt(client.getCreatedAt());
        response.setUpdatedAt(client.getUpdatedAt());

        return response;
    }

    private UserSummaryDto mapToUserSummary(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getEmail());
    }
}
