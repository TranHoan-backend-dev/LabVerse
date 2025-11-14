package com.se1853_jv.repository

import com.se1853_jv.model.Favorite
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FavoriteRepository : MongoRepository<Favorite, String> {
    fun existsByPaperIdAndUserId(paperId: String, userId: String): Boolean
    fun findByUserId(userId: String): List<Favorite>
    fun findByPaperId(paperId: String): List<Favorite>
    fun deleteByPaperIdAndUserId(paperId: String, userId: String)
}

