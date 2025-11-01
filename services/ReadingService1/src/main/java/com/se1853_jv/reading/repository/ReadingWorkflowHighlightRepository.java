package com.se1853_jv.reading.repository;

import com.se1853_jv.reading.model.ReadingWorkflowHighlight;
import com.se1853_jv.reading.model.ReadingWorkflowHighlightId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingWorkflowHighlightRepository extends JpaRepository<ReadingWorkflowHighlight, ReadingWorkflowHighlightId> {

    List<ReadingWorkflowHighlight> findByIdCollectionIdAndIdPaperIdAndIdUserId(String collectionId, String paperId, String userId);

    Optional<ReadingWorkflowHighlight> findByIdCollectionIdAndIdPaperIdAndIdUserIdAndIdHighlightId(String collectionId, String paperId, String userId, String highlightId);
}

