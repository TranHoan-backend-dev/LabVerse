package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.ReadingWorkflowNote;
import com.se1853_jv.readingservice.model.ReadingWorkflowNoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingWorkflowNoteRepository extends JpaRepository<ReadingWorkflowNote, ReadingWorkflowNoteId> {

    List<ReadingWorkflowNote> findById_CollectionIdAndId_PaperIdAndId_UserId(String collectionId, String paperId, String userId);

    Optional<ReadingWorkflowNote> findById_CollectionIdAndId_PaperIdAndId_UserIdAndId_NoteId(String collectionId, String paperId, String userId, String noteId);
    
    void deleteById_NoteId(String noteId);
}

