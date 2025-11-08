package com.se1853_jv.repository

import com.se1853_jv.model.ReadingWorkflow
import com.se1853_jv.model.ReadingWorkflowId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReadingWorkflowRepository : JpaRepository<ReadingWorkflow, ReadingWorkflowId> {
    
    /**
     * Find ReadingWorkflow by composite key
     */
    fun findByCollectionIdAndPaperIdAndUserId(
        collectionId: String,
        paperId: String,
        userId: String
    ): ReadingWorkflow?
    
    /**
     * Find all ReadingWorkflows for a paper in a collection
     */
    fun findByCollectionIdAndPaperId(
        collectionId: String,
        paperId: String
    ): List<ReadingWorkflow>
}







