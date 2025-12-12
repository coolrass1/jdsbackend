package com.skk.jdsbackend.repository;

import com.skk.jdsbackend.entity.Note;
import com.skk.jdsbackend.entity.Case;
import com.skk.jdsbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByCaseEntity(Case caseEntity);

    List<Note> findByAuthor(User author);

    List<Note> findByCaseEntityOrderByCreatedAtDesc(Case caseEntity);
}
