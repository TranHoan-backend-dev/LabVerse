package com.se1853_jv.service.impl

import com.google.cloud.firestore.Firestore
import com.se1853_jv.dto.request.UploadPdfRequest
import com.se1853_jv.dto.response.PaginatedPapersListResponse
import com.se1853_jv.dto.response.PaperResponse
import com.se1853_jv.model.*
import com.se1853_jv.repository.PaperRepository
import com.se1853_jv.repository.TagRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.PaperService
import mu.KotlinLogging
import org.springframework.cloud.context.named.NamedContextFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
    private val db: Firestore?,
) : PaperService {

    override fun getPaperDetails(paperId: String): PaperResponse {
        logger.info { "Fetching details for paperId=$paperId" }
        val paper = paperRepo.findById(paperId).orElseThrow { IllegalArgumentException("Paper not found") }
        val response = convert(paper)

        if (db != null) {
            try {
                val data = storeData(response)
                db.collection(COLLECTION_NAME).add(data)
            } catch (e: Exception) {
                logger.error(e) { "Failed to send paper to Firebase: ${e.message}" }
            }
        } else {
            logger.debug { "Firebase not available, skipping Firebase sync for paper" }
        }

        return response
    }

    override fun getAllPapers(
        searchQuery: String?, pageIndex: Int, pageSize: Int?,
        author: String?, journal: String?, publicationYearFrom: Int?,
        publicationYearTo: Int?
    ): PaginatedPapersListResponse {
        logger.info {
            "Getting all papers with search query: $searchQuery, pageIndex: $pageIndex, pageSize: $pageSize," +
                    " author: $author, journal: $journal, publicationYearFrom: $publicationYearFrom"
        }
        val data = paperRepo.searchPapers(
            searchQuery,
            author,
            journal,
            publicationYearFrom,
            publicationYearTo,
            pageIndex,
            pageSize ?: PAGE_SIZE
        )

        val response = data.map { convert(it) }
        return PaginatedPapersListResponse(
            response.content,
            data.totalPages,
            data.totalElements
        )
    }

    override fun deleteById(id: String) {
        logger.info { "Deleting paper with id: $id" }
        paperRepo.findById(id).orElseThrow { IllegalArgumentException("Paper not found") }
        paperRepo.deleteById(id)
    }

    override fun createNewPaper(req: UploadPdfRequest, userId: String?) {
        logger.info { "Create new paper with title: ${req.title}, userId: $userId" }

        // Generate DOI if not provided or empty
        val finalDoi = if (req.doi.isNullOrBlank()) {
            generateUniqueDoi()
        } else {
            // Check if DOI already exists
            if (paperRepo.existsByMetadataDoi(req.doi)) {
                throw IllegalArgumentException("Doi is existing")
            }
            req.doi
        }

        val tagIds = mutableListOf<String>()
        if (!req.tags.isNullOrEmpty()) {
            req.tags.forEach {
                val tag = tagRepo.findByName(it)
                tag?.id?.let { tagId ->
                    tagIds.add(tagId)
                }
            }
        }

        val paper = buildEntity(
            req.dataUrl,
            req.description,
            req.keywords,
            Metadata(req.title, req.authors, req.journal, req.publicationYear, finalDoi),
            tagIds,
            userId,
        )

        logger.info { "Saving paper with createdBy: ${paper.createdBy}, DOI: $finalDoi" }
        val savedPaper = paperRepo.save(paper)
        logger.info { "Paper saved successfully with id: ${savedPaper.id}, createdBy: ${savedPaper.createdBy}" }
    }

    /**
     * Generate a unique DOI in format: 10.0000/{unique-id}
     * Uses UUID and timestamp to ensure uniqueness
     */
    private fun generateUniqueDoi(): String {
        val uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        val timestamp = System.currentTimeMillis().toString().takeLast(8)
        // Format: 10.0000/{unique-id}
        // Using 10.0000 as prefix for auto-generated DOIs
        val doi = "10.0000/labverse.$uniqueId.$timestamp"
        
        // Ensure DOI doesn't already exist (very unlikely but check anyway)
        var attempts = 0
        var finalDoi = doi
        while (paperRepo.existsByMetadataDoi(finalDoi) && attempts < 5) {
            val newUniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
            finalDoi = "10.0000/labverse.$newUniqueId.${System.currentTimeMillis()}"
            attempts++
        }
        
        if (attempts >= 5) {
            logger.warn { "Failed to generate unique DOI after 5 attempts, using timestamp-based DOI" }
            finalDoi = "10.0000/labverse.${System.currentTimeMillis()}"
        }
        
        return finalDoi
    }

    override fun getPapersByUserId(userId: String): List<PaperResponse> {
        logger.info { "Getting papers for userId: $userId" }
        val papers = paperRepo.findByCreatedByOrderByIdDesc(userId)
        logger.info { "Found ${papers.size} papers in database for userId: $userId" }
        papers.forEach { paper ->
            logger.debug { "Paper: id=${paper.id}, title=${paper.metadata?.title}, createdBy=${paper.createdBy}" }
        }
        return papers.map { convert(it) }
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

    private fun buildEntity(
        dataUrl: String,
        description: String?,
        keywords: List<String>?,
        metadata: Metadata,
        tagIds: List<String>?,
        createdBy: String? = null,
    ) = Paper(
        id = UUID.randomUUID().toString(),
        dataUrl = dataUrl,
        description = description,
        keywords = keywords,
        metadata = metadata,
        tagIds = tagIds ?: emptyList(),
        createdBy = createdBy,
    )
}