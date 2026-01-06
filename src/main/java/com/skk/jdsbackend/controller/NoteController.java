package com.skk.jdsbackend.controller;

import com.skk.jdsbackend.dto.MessageResponse;
import com.skk.jdsbackend.dto.NoteCreateRequest;
import com.skk.jdsbackend.dto.NoteResponse;
import com.skk.jdsbackend.dto.NoteUpdateRequest;
import com.skk.jdsbackend.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteCreateRequest request) {
        NoteResponse response = noteService.createNote(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        NoteResponse response = noteService.getNoteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteResponse>> getAllNotes() {
        List<NoteResponse> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteResponse>> getNotesByCaseId(@PathVariable Long caseId) {
        List<NoteResponse> notes = noteService.getNotesByCaseId(caseId);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/author/{authorId}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteResponse>> getNotesByAuthor(@PathVariable Long authorId) {
        List<NoteResponse> notes = noteService.getNotesByAuthor(authorId);
        return ResponseEntity.ok(notes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteUpdateRequest request) {
        NoteResponse response = noteService.updateNote(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CASE_WORKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok(new MessageResponse("Note deleted successfully"));
    }
}
