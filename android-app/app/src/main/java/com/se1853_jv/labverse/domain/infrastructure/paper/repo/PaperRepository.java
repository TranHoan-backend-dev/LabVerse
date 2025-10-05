package com.se1853_jv.labverse.domain.infrastructure.paper.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

@Dao
public interface PaperRepository {
    @Insert
    void create(PaperResearch paper);

    @Update
    void update(PaperResearch paper);

    @Delete
    void delete(PaperResearch paper);

    @Query("DELETE FROM PaperResearch WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM PaperResearch WHERE id = :id")
    PaperResearch getById(String id);
}
