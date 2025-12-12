package com.skk.jdsbackend.service;

import com.skk.jdsbackend.dto.NoteCreateRequest;
import com.skk.jdsbackend.dto.NoteResponse;
import com.skk.jdsbackend.dto.NoteUpdateRequest;
import com.skk.jdsbackend.dto.UserSummaryDto;
import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.Note;
import com.skk.jdsbackend.entity.User;
import com.skk.jdsbackend.exception.ResourceNotFoundException;
import com.skk.jdsbackend.repository.CaseRepository;
import com.skk.jdsbackend.repository.NoteRepository;
import com.skk.jdsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;

    @Transactional
    public NoteResponse createNote(NoteCreateRequest request) {
        Case caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + request.getCaseId()));

        User currentUser = getCurrentUser();

        Note note = new Note();
        note.setContent(request.getContent());
        note.setCaseEntity(caseEntity);
        note.setAuthor(currentUser);

        Note savedNote = noteRepository.save(note);
        return mapToResponse(savedNote);
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
        return mapToResponse(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByCaseId(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with id: " + caseId));
        return noteRepository.findByCaseEntityOrderByCreatedAtDesc(caseEntity).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + authorId));
        return noteRepository.findByAuthor(author).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NoteResponse updateNote(Long id, NoteUpdateRequest request) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        // Verify that the current user is the author
        User currentUser = getCurrentUser();
        if (!note.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own notes");
        }

        note.setContent(request.getContent());
        Note updatedNote = noteRepository.save(note);
        return mapToResponse(updatedNote);
    }

    @Transactional
    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));

        // Verify that the current user is the author
        User currentUser = getCurrentUser();
        if (!note.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own notes");
        }

        noteRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private NoteResponse mapToResponse(Note note) {
        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setContent(note.getContent());
        response.setCaseId(note.getCaseEntity().getId());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());

        if (note.getAuthor() != null) {
            UserSummaryDto authorSummary = new UserSummaryDto();
            authorSummary.setId(note.getAuthor().getId());
            authorSummary.setUsername(note.getAuthor().getUsername());
            authorSummary.setEmail(note.getAuthor().getEmail());
            response.setAuthor(authorSummary);
        }

        return response;
    }
}
