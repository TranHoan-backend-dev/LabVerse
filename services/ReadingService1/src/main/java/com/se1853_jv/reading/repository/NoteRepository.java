package com.se1853_jv.reading.repository;

import com.se1853_jv.reading.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, String> {
}

