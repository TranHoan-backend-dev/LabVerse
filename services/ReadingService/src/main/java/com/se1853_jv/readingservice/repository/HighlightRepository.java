package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, String> {
    
    // Query through Many-to-Many relationship via ReadingWorkflow
    @Query("SELECT DISTINCT h FROM ReadingWorkflow rw " +
           "JOIN rw.highlights h " +
           "WHERE rw.id.collectionId = :collectionId " +
           "AND rw.id.paperId = :paperId " +
           "AND rw.id.usersid = :usersid")
    List<Highlight> findByCollectionIdAndPaperIdAndUsersid(
            @Param("collectionId") String collectionId,
            @Param("paperId") String paperId,
            @Param("usersid") String usersid);
}

