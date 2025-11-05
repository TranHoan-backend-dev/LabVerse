package com.se1853_jv.repository

import com.se1853_jv.model.Reference
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ReferenceRepository : MongoRepository<Reference, String> {
    fun findByPaperIdsContaining(papers: String): List<Reference>
}