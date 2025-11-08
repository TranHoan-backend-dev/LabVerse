package com.se1853_jv.repository

import com.se1853_jv.model.Tag
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TagRepository : MongoRepository<Tag, String> {
    fun findByPaperIdsContaining(papers: String): List<Tag>
    fun findByName(name: String): Tag?
}