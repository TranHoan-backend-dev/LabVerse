package com.se1853_jv.repository

import com.se1853_jv.model.ReadingWorkflowHighlight
import com.se1853_jv.model.ReadingWorkflowHighlightId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReadingWorkflowHighlightRepository : JpaRepository<ReadingWorkflowHighlight, ReadingWorkflowHighlightId> {
    
    /**
     * Delete all highlights for a specific reading workflow
     */
    fun deleteByRwCollectionIdAndRwPaperIdAndRwUserId(
        collectionId: String,
        paperId: String,
        userId: String
    )
}







