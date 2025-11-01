package com.se1853_jv.reading.repository;

import com.se1853_jv.reading.model.ReadingWorkflowNote;
import com.se1853_jv.reading.model.ReadingWorkflowNoteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingWorkflowNoteRepository extends JpaRepository<ReadingWorkflowNote, ReadingWorkflowNoteId> {

    List<ReadingWorkflowNote> findByIdCollectionIdAndIdPaperIdAndIdUserId(String collectionId, String paperId, String userId);

    Optional<ReadingWorkflowNote> findByIdCollectionIdAndIdPaperIdAndIdUserIdAndIdNoteId(String collectionId, String paperId, String userId, String noteId);
}

