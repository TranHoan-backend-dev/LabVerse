package com.se1853_jv.repository

import com.se1853_jv.model.Paper
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class PaperRepositoryCustomImpl(
    private val mongoTemplate: MongoTemplate
) : PaperRepositoryCustom {
    override fun searchPapers(
        searchQuery: String?,
        author: String?,
        journal: String?,
        publicationYearFrom: Int?,
        publicationYearTo: Int?,
        pageIndex: Int,
        pageSize: Int
    ): Page<Paper> {

        val criteriaList = mutableListOf<Criteria>()

        if (!searchQuery.isNullOrBlank()) {
            criteriaList.add(
                Criteria().orOperator(
                    Criteria.where("metadata.title").regex(searchQuery, "i"),
                    Criteria.where("metadata.authors").regex(searchQuery, "i"),
                    Criteria.where("metadata.journal").regex(searchQuery, "i"),
                    Criteria.where("metadata.keywords").regex(searchQuery, "i")
                )
            )
        }

        if (!author.isNullOrBlank()) {
            criteriaList.add(Criteria.where("metadata.authors").regex(author, "i"))
        }

        if (!journal.isNullOrBlank()) {
            criteriaList.add(Criteria.where("metadata.journal").regex(journal, "i"))
        }

        if (publicationYearFrom != null || publicationYearTo != null) {
            val yearCriteria = Criteria.where("metadata.publicationYear")
            publicationYearFrom?.let { yearCriteria.gte(it) }
            publicationYearTo?.let { yearCriteria.lte(it) }
            criteriaList.add(yearCriteria)
        }

        val query = if (criteriaList.isNotEmpty())
            Query(Criteria().andOperator(*criteriaList.toTypedArray()))
        else Query()

        val pageable = PageRequest.of(pageIndex, pageSize)
        val total = mongoTemplate.count(query, Paper::class.java)

        query.with(pageable)

        val results = mongoTemplate.find(query, Paper::class.java)

        return PageImpl(results, pageable, total)
    }
}