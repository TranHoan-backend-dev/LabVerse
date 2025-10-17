package com.se1853_jv.service.impl

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.se1853_jv.dto.response.PaperResponse
import com.se1853_jv.model.Paper
import com.se1853_jv.repository.PaperRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.PaperService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}
private const val COLLECTION_NAME: String = "paper"

@Service
class PaperServiceImpl(
    private val repo: PaperRepository,
    private val encoder: EncoderService,
    private val db: Firestore = FirestoreClient.getFirestore(),
) : PaperService {
    override fun getPaperDetails(paperId: String): PaperResponse {
        logger.info { "Fetching details for paperId=$paperId" }
        val paper = repo.findById(paperId).orElseThrow { IllegalArgumentException("Paper not found") }
        val response = convert(paper)

        val data = storeData(response)
        db.collection(COLLECTION_NAME).add(data)

        return response
    }

    private fun convert(paper: Paper): PaperResponse {
        return PaperResponse(
            id = encoder.encode(paper.id!!),
            dataUrl = paper.dataUrl ?: "",
            keywords = paper.keywords,
            title = paper.metadata?.title ?: "",
            authors = paper.metadata?.title ?: "",
            journal = paper.metadata?.journal ?: "",
            publicationYear = paper.metadata?.publicationYear ?: 0,
            doi = paper.metadata?.doi ?: "",
        )
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
}