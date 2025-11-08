package com.se1853_jv.service.impl

import com.google.cloud.firestore.Firestore
import com.se1853_jv.dto.request.SearchPapersRequest
import com.se1853_jv.dto.request.UploadPdfRequest
import com.se1853_jv.dto.response.PaperResponse
import com.se1853_jv.model.*
import com.se1853_jv.repository.PaperRepository
import com.se1853_jv.repository.TagRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.PaperService
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.util.UUID

private val logger = KotlinLogging.logger {}
private const val COLLECTION_NAME: String = "paper"
private const val PAGE_SIZE: Int = 5

@Service
class PaperServiceImpl(
    private val paperRepo: PaperRepository,
    private val encoder: EncoderService,
    private val tagRepo: TagRepository,
    private val db: Firestore,
    private val mongoTemplate: MongoTemplate,
) : PaperService {

    override fun getPaperDetails(paperId: String): PaperResponse {
        logger.info { "Fetching details for paperId=$paperId" }
        val paper = paperRepo.findById(paperId).orElseThrow { IllegalArgumentException("Paper not found") }
        val response = convert(paper)

        val data = storeData(response)
        db.collection(COLLECTION_NAME).add(data)

        return response
    }

    override fun getAllPapers(searchQuery: String?, pageIndex: Int, pageSize: Int?, tagIds: List<String>?): List<PaperResponse> {
        logger.info { "Getting all papers with search query: $searchQuery, tagIds: $tagIds" }
        
        // If tagIds is provided, use MongoTemplate for efficient querying
        if (!tagIds.isNullOrEmpty()) {
            val queryConditions = mutableListOf<Criteria>()
            
            // Tags filter - papers must have at least one of the provided tagIds
            queryConditions.add(Criteria("tagIds").`in`(tagIds))
            
            // General query search (searches across multiple fields)
            if (!searchQuery.isNullOrBlank()) {
                val searchPattern = ".*${Regex.escape(searchQuery)}.*"
                val regex = Regex(searchPattern, RegexOption.IGNORE_CASE)
                queryConditions.add(
                    Criteria().orOperator(
                        Criteria("metadata.title").regex(regex.pattern, "i"),
                        Criteria("metadata.authors").regex(regex.pattern, "i"),
                        Criteria("metadata.journal").regex(regex.pattern, "i"),
                        Criteria("keywords").regex(regex.pattern, "i"),
                        Criteria("description").regex(regex.pattern, "i")
                    )
                )
            }
            
            val mongoQuery = Query(Criteria().andOperator(*queryConditions.toTypedArray()))
            
            // Pagination
            val pageRequest = PageRequest.of(pageIndex, pageSize ?: PAGE_SIZE)
            mongoQuery.with(pageRequest)
            
            // Default sort by publication year descending
            mongoQuery.with(Sort.by(Sort.Direction.DESC, "metadata.publicationYear"))
            
            val papers = mongoTemplate.find(mongoQuery, Paper::class.java)
            logger.info { "Found ${papers.size} papers matching filters" }
            
            return papers.map { convert(it) }
        }
        
        // If no tagIds, use the original simple filtering approach
        val allPapers: Page<Paper> = paperRepo.findAll(PageRequest.of(pageIndex, pageSize ?: PAGE_SIZE))
        val data = allPapers.content

        val filteredPapers = if (searchQuery.isNullOrBlank()) {
            data
        } else {
            val query = searchQuery.lowercase()
            data.filter { paper ->
                paper.metadata?.title?.lowercase()?.contains(query) == true ||
                        paper.metadata?.authors?.lowercase()?.contains(query) == true ||
                        paper.metadata?.journal?.lowercase()?.contains(query) == true ||
                        paper.keywords?.any { it.lowercase().contains(query) } == true
            }
        }

        return filteredPapers.map { convert(it) }
    }

    override fun searchPapersWithFilters(request: SearchPapersRequest): List<PaperResponse> {
        logger.info { "Searching papers with filters: $request" }
        
        // Validate that if string fields are provided, they are not blank
        validateSearchRequest(request)
        
        val queryConditions = mutableListOf<Criteria>()
        
        // General query search (searches across multiple fields)
        if (!request.query.isNullOrBlank()) {
            val searchPattern = ".*${Regex.escape(request.query)}.*"
            val regex = Regex(searchPattern, RegexOption.IGNORE_CASE)
            queryConditions.add(
                Criteria().orOperator(
                    Criteria("metadata.title").regex(regex.pattern, "i"),
                    Criteria("metadata.authors").regex(regex.pattern, "i"),
                    Criteria("metadata.journal").regex(regex.pattern, "i"),
                    Criteria("keywords").regex(regex.pattern, "i"),
                    Criteria("description").regex(regex.pattern, "i")
                )
            )
        }
        
        // Specific field filters
        if (!request.title.isNullOrBlank()) {
            queryConditions.add(
                Criteria("metadata.title").regex(".*${Regex.escape(request.title)}.*", "i")
            )
        }
        
        if (!request.authors.isNullOrBlank()) {
            queryConditions.add(
                Criteria("metadata.authors").regex(".*${Regex.escape(request.authors)}.*", "i")
            )
        }
        
        if (!request.journal.isNullOrBlank()) {
            queryConditions.add(
                Criteria("metadata.journal").regex(".*${Regex.escape(request.journal)}.*", "i")
            )
        }
        
        if (!request.doi.isNullOrBlank()) {
            queryConditions.add(
                Criteria("metadata.doi").regex(".*${Regex.escape(request.doi)}.*", "i")
            )
        }
        
        // Keywords filter (at least one keyword in request must match at least one keyword in paper)
        if (!request.keywords.isNullOrEmpty()) {
            val keywordCriteria = request.keywords.map { keyword ->
                Criteria("keywords").regex(".*${Regex.escape(keyword)}.*", "i")
            }
            if (keywordCriteria.isNotEmpty()) {
                queryConditions.add(Criteria().orOperator(*keywordCriteria.toTypedArray()))
            }
        }
        
        // Year range filter
        if (request.yearFrom != null || request.yearTo != null) {
            val yearCriteria = Criteria("metadata.publicationYear")
            if (request.yearFrom != null && request.yearTo != null) {
                yearCriteria.gte(request.yearFrom).lte(request.yearTo)
            } else if (request.yearFrom != null) {
                yearCriteria.gte(request.yearFrom)
            } else if (request.yearTo != null) {
                yearCriteria.lte(request.yearTo)
            }
            queryConditions.add(yearCriteria)
        }
        
        // Tags filter
        if (!request.tagIds.isNullOrEmpty()) {
            queryConditions.add(Criteria("tagIds").`in`(request.tagIds))
        }
        
        // Combine all conditions with AND operator
        val mongoQuery = if (queryConditions.isNotEmpty()) {
            val finalCriteria = Criteria().andOperator(*queryConditions.toTypedArray())
            Query(finalCriteria)
        } else {
            // No filters, return all papers
            Query()
        }
        
        // Pagination
        val pageRequest = PageRequest.of(request.pageIndex, request.pageSize)
        mongoQuery.with(pageRequest)
        
        // Sorting
        if (!request.sortBy.isNullOrBlank()) {
            val sortDirection = if (request.sortOrder == "desc") {
                Sort.Direction.DESC
            } else {
                Sort.Direction.ASC
            }
            
            when (request.sortBy.lowercase()) {
                "title" -> mongoQuery.with(Sort.by(sortDirection, "metadata.title"))
                "publicationyear", "year" -> mongoQuery.with(Sort.by(sortDirection, "metadata.publicationYear"))
                "authors" -> mongoQuery.with(Sort.by(sortDirection, "metadata.authors"))
                "journal" -> mongoQuery.with(Sort.by(sortDirection, "metadata.journal"))
                else -> mongoQuery.with(Sort.by(Sort.Direction.DESC, "metadata.publicationYear"))
            }
        } else {
            // Default sort by publication year descending
            mongoQuery.with(Sort.by(Sort.Direction.DESC, "metadata.publicationYear"))
        }
        
        val papers = mongoTemplate.find(mongoQuery, Paper::class.java)
        
        logger.info { "Found ${papers.size} papers matching filters" }
        
        return papers.map { convert(it) }
    }

    override fun deleteById(id: String) {
        logger.info { "Deleting paper with id: $id" }
        paperRepo.findById(id).orElseThrow { IllegalArgumentException("Paper not found") }
        paperRepo.deleteById(id)
    }

    override fun createNewPaper(req: UploadPdfRequest) {
        logger.info { "Create new paper with title: ${req.title}" }

        if (paperRepo.existsByMetadataDoi(req.doi)) {
            throw IllegalArgumentException("Doi is existing")
        }

        val tagIds = emptyList<String>()
        if (!req.tags.isNullOrEmpty()) {
            req.tags.forEach {
                val tag = tagRepo.findByName(it)
                if (tag != null) {
                    tagIds.plus(tag.id)
                }
            }
        }

        val paper = buildEntity(
            req.dataUrl,
            req.description,
            req.keywords,
            Metadata(req.title, req.authors, req.journal, req.publicationYear, req.doi),
            tagIds,
        )

        paperRepo.save(paper)
    }

    private fun storeData(item: PaperResponse): MutableMap<String, Any> {
        val response: MutableMap<String, Any> = HashMap()
        response["id"] = item.id
        response["dataUrl"] = item.dataUrl
        response["keywords"] = item.keywords ?: emptyList<String>()
        response["title"] = item.title
        response["authors"] = item.authors
        response["journal"] = item.journal
        response["publicationYear"] = item.publicationYear
        response["doi"] = item.doi
        return response
    }

    private fun convert(paper: Paper): PaperResponse {
        return PaperResponse(
            id = encoder.encode(paper.id!!),
            dataUrl = paper.dataUrl ?: "",
            keywords = paper.keywords,
            title = paper.metadata?.title ?: "",
            authors = paper.metadata?.authors ?: "",
            journal = paper.metadata?.journal ?: "",
            publicationYear = paper.metadata?.publicationYear ?: 0,
            doi = paper.metadata?.doi ?: "",
            description = paper.description,
        )
    }

    private fun validateSearchRequest(request: SearchPapersRequest) {
        // Validate string fields: if provided, must not be blank
        if (request.query != null && request.query.isBlank()) {
            throw IllegalArgumentException("Query cannot be blank if provided")
        }
        if (request.title != null && request.title.isBlank()) {
            throw IllegalArgumentException("Title cannot be blank if provided")
        }
        if (request.authors != null && request.authors.isBlank()) {
            throw IllegalArgumentException("Authors cannot be blank if provided")
        }
        if (request.journal != null && request.journal.isBlank()) {
            throw IllegalArgumentException("Journal cannot be blank if provided")
        }
        
        // Validate keywords: if provided, must not be empty and each keyword must not be blank
        if (!request.keywords.isNullOrEmpty()) {
            if (request.keywords.any { it.isBlank() }) {
                throw IllegalArgumentException("Keywords cannot contain blank values")
            }
        }
        
        // Validate tagIds: if provided, must not be empty and each tagId must not be blank
        if (!request.tagIds.isNullOrEmpty()) {
            if (request.tagIds.any { it.isBlank() }) {
                throw IllegalArgumentException("Tag IDs cannot contain blank values")
            }
        }
        
        // Validate year range: yearTo must be >= yearFrom
        if (request.yearFrom != null && request.yearTo != null) {
            if (request.yearTo < request.yearFrom) {
                throw IllegalArgumentException("Year to must be greater than or equal to year from")
            }
        }
        
        // Validate sortOrder
        if (!request.sortOrder.isNullOrBlank()) {
            val sortOrder = request.sortOrder.lowercase()
            if (sortOrder != "asc" && sortOrder != "desc") {
                throw IllegalArgumentException("Sort order must be 'asc' or 'desc'")
            }
        }
    }

    private fun buildEntity(
        dataUrl: String,
        description: String?,
        keywords: List<String>?,
        metadata: Metadata,
        tagIds: List<String>?,
    ) = Paper(
        id = UUID.randomUUID().toString(),
        dataUrl = dataUrl,
        description = description,
        keywords = keywords,
        metadata = metadata,
        tagIds = tagIds ?: emptyList(),
    )
}