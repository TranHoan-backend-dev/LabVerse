package com.se1853_jv.labverse.domain.infrastructure.citation.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;

@Dao
public interface CitationRepository {
    @Insert
    void create(Citation citation);

    @Update
    void update(Citation citation);

    @Delete
    void delete(Citation citation);

    @Query("DELETE FROM Citation WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Citation WHERE id = :id")
    Citation getById(String id);
}
