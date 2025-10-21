package com.se1853_jv.repository

import com.se1853_jv.model.Citation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CitationRepository : MongoRepository<Citation, String> {
    fun findByPaperIdsContaining(papers: String): List<Citation>
}