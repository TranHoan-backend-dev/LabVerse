package com.se1853_jv.labverse.domain.infrastructure.annotation.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;

@Dao
public interface HighlightRepository {
    @Insert
    void create(Highlight highlight);

    @Update
    void update(Highlight highlight);

    @Delete
    void delete(Highlight highlight);

    @Query("DELETE FROM Highlight WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Highlight WHERE id = :id")
    Highlight getById(String id);
}
