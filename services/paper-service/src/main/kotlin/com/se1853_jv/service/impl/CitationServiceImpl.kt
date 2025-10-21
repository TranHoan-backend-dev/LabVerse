package com.se1853_jv.service.impl

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.se1853_jv.dto.response.CitationResponse
import com.se1853_jv.model.Citation
import com.se1853_jv.repository.CitationRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.CitationService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}
private const val COLLECTION_NAME: String = "citation"

@Service
class CitationServiceImpl(
    private val repo: CitationRepository,
    private val encoder: EncoderService,
    private val db: Firestore,
) : CitationService {

    override fun getCitationsByPaperId(id: String): List<CitationResponse> {
        logger.info { "Get citations with paper id $id" }
        val list = repo.findByPaperIdsContaining(id).map { convert(it) }

        val result: MutableList<Map<String, Any>> = ArrayList()
        list.forEach { item ->
            result.add(storeData(item))
        }

        result.forEach { db.collection(COLLECTION_NAME).add(it) }

        return list
    }

    override fun getCitationById(id: String): CitationResponse {
        logger.info { "Get details with citation id: $id" }
        val citation = repo.findById(id).orElseThrow { RuntimeException("Citation not found") }
        val response = convert(citation)

        val data: ApiFuture<DocumentReference> = db.collection(COLLECTION_NAME).add(storeData(response))
        logger.info { "Data: ${data.get().id}" }

        return response
    }

    fun convert(citation: Citation): CitationResponse {
        return CitationResponse(
            id = encoder.encode(citation.id),
            publicationYear = citation.metadata?.publicationYear ?: 0,
            doi = citation.metadata?.doi ?: "",
            title = citation.metadata?.title ?: "",
            authors = citation.metadata?.authors ?: "",
            journal = citation.metadata?.journal ?: "",
        )
    }

    fun storeData(item: CitationResponse): Map<String, Any> {
        val citation: MutableMap<String, Any> = HashMap()
        citation["id"] = item.id
        citation["publicationYear"] = item.publicationYear
        citation["doi"] = item.doi
        citation["title"] = item.title
        citation["authors"] = item.authors
        citation["journal"] = item.journal
        return citation
    }

}