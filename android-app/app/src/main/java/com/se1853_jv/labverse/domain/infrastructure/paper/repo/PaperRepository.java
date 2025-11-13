package com.se1853_jv.labverse.domain.infrastructure.paper.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.List;

@Dao
public interface PaperRepository {
    @Insert
    void create(PaperResearch paper);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(PaperResearch paper);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateAll(List<PaperResearch> papers);

    @Update
    void update(PaperResearch paper);

    @Delete
    void delete(PaperResearch paper);

    @Query("DELETE FROM PaperResearch WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM PaperResearch WHERE id = :id")
    PaperResearch getById(String id);

    @Query("SELECT * FROM PaperResearch ORDER BY id DESC")
    List<PaperResearch> getAllPapers();

    @Query("SELECT * FROM PaperResearch ORDER BY id DESC LIMIT :limit")
    List<PaperResearch> getRecentlyAdded(int limit);

    @Query("SELECT COUNT(*) FROM PaperResearch")
    int getPaperCount();
}
