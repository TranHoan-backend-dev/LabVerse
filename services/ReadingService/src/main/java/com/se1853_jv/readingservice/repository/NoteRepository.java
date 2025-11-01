package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    
    @Query("SELECT n FROM Note n " +
           "INNER JOIN ReadingWorkflowNote rwn ON CAST(n.id AS string) = rwn.id.noteId " +
           "WHERE rwn.id.collectionId = :collectionId " +
           "AND rwn.id.paperId = :paperId " +
           "AND rwn.id.userId = :userId")
    List<Note> findByCollectionIdAndPaperIdAndUserId(
            @Param("collectionId") String collectionId,
            @Param("paperId") String paperId,
            @Param("userId") String userId);
}

