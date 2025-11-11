package com.se1853_jv.labverse.domain.infrastructure.workflow.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

@Dao
public interface ReadingWorkflowRepository {
    @Insert
    void create(ReadingWorkflow workflow);

    @Update
    void update(ReadingWorkflow workflow);

    @Delete
    void deleteById(ReadingWorkflow workflow);

    @Query("SELECT * FROM ReadingWorkflow WHERE userId = :userId AND paperId = :paperId AND collectionId = :collectionId")
    ReadingWorkflow getByCompositeKey(String userId, String paperId, String collectionId);
}
