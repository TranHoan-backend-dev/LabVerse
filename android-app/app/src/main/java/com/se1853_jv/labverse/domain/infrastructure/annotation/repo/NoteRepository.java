package com.se1853_jv.labverse.domain.infrastructure.annotation.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;

@Dao
public interface NoteRepository {
    @Insert
    void create(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM Note WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Note WHERE id = :id")
    Note getById(String id);
}
