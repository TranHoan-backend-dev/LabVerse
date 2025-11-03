package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {
    
    // Query through Many-to-Many relationship via ReadingWorkflow
    @Query("SELECT DISTINCT n FROM ReadingWorkflow rw " +
           "JOIN rw.notes n " +
           "WHERE rw.id.collectionId = :collectionId " +
           "AND rw.id.paperId = :paperId " +
           "AND rw.id.usersid = :usersid")
    List<Note> findByCollectionIdAndPaperIdAndUsersid(
            @Param("collectionId") String collectionId,
            @Param("paperId") String paperId,
            @Param("usersid") String usersid);
}

