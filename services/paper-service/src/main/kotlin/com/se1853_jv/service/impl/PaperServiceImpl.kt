package com.se1853_jv.service.impl

import com.google.cloud.firestore.Firestore
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
) : PaperService {

    override fun getPaperDetails(paperId: String): PaperResponse {
        logger.info { "Fetching details for paperId=$paperId" }
        val paper = paperRepo.findById(paperId).orElseThrow { IllegalArgumentException("Paper not found") }
        val response = convert(paper)

        val data = storeData(response)
        db.collection(COLLECTION_NAME).add(data)

        return response
    }

    override fun getAllPapers(searchQuery: String?, pageIndex: Int, pageSize: Int?): List<PaperResponse> {
        logger.info { "Getting all papers with search query: $searchQuery" }
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