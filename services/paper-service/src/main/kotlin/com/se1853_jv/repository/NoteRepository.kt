package com.se1853_jv.repository

import com.se1853_jv.model.Note
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : MongoRepository<Note, String> {
    
    /**
     * Find notes by paper ID
     */
    fun findByPaperId(paperId: String): List<Note>
    
    /**
     * Find notes by paper ID and collection ID
     */
    fun findByPaperIdAndCollectionId(paperId: String, collectionId: String): List<Note>
    
    /**
     * Find notes by user ID, paper ID, and collection ID
     */
    fun findByUserIdAndPaperIdAndCollectionId(
        userId: String,
        paperId: String,
        collectionId: String
    ): List<Note>
    
    /**
     * Find notes by paper ID and user ID (for personal collections)
     */
    fun findByPaperIdAndUserId(paperId: String, userId: String): List<Note>
}







