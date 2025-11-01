package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, UUID> {
    
    @Query("SELECT h FROM Highlight h " +
           "INNER JOIN ReadingWorkflowHighlight rwh ON CAST(h.id AS string) = rwh.id.highlightId " +
           "WHERE rwh.id.collectionId = :collectionId " +
           "AND rwh.id.paperId = :paperId " +
           "AND rwh.id.userId = :userId")
    List<Highlight> findByCollectionIdAndPaperIdAndUserId(
            @Param("collectionId") String collectionId,
            @Param("paperId") String paperId,
            @Param("userId") String userId);
}

