package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.ReadingWorkflowHighlight;
import com.se1853_jv.readingservice.model.ReadingWorkflowHighlightId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingWorkflowHighlightRepository extends JpaRepository<ReadingWorkflowHighlight, ReadingWorkflowHighlightId> {

    List<ReadingWorkflowHighlight> findById_CollectionIdAndId_PaperIdAndId_UserId(String collectionId, String paperId, String userId);

    Optional<ReadingWorkflowHighlight> findById_CollectionIdAndId_PaperIdAndId_UserIdAndId_HighlightId(String collectionId, String paperId, String userId, String highlightId);
    
    void deleteById_HighlightId(String highlightId);
}

