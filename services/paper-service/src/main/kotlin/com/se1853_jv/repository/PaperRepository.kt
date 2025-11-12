package com.se1853_jv.repository

import com.se1853_jv.model.Paper
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PaperRepository : MongoRepository<Paper, String>, PaperRepositoryCustom {
    fun existsByMetadataDoi(doi: String): Boolean
    fun findByCreatedByOrderByIdDesc(userId: String): List<Paper>
}