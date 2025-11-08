package com.se1853_jv.repository

import com.se1853_jv.model.ReadingWorkflowNote
import com.se1853_jv.model.ReadingWorkflowNoteId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReadingWorkflowNoteRepository : JpaRepository<ReadingWorkflowNote, ReadingWorkflowNoteId> {
    
    /**
     * Delete all notes for a specific reading workflow
     */
    fun deleteByRwCollectionIdAndRwPaperIdAndRwUserId(
        collectionId: String,
        paperId: String,
        userId: String
    )
}







