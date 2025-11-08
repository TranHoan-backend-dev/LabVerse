package com.se1853_jv.service.impl

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.se1853_jv.dto.response.ReferenceResponse
import com.se1853_jv.model.Reference
import com.se1853_jv.repository.ReferenceRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.ReferenceService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}
private const val COLLECTION_NAME: String = "reference"

@Service
class ReferenceServiceImpl(
    private val repo: ReferenceRepository,
    private val encoder: EncoderService,
    private val db: Firestore?,
) : ReferenceService {

    override fun getReferencesByPaperId(id: String): List<ReferenceResponse> {
        logger.info { "Get references with paper id $id" }
        val list = repo.findByPaperIdsContaining(id).map { convert(it) }

        if (db != null) {
            try {
                val result: MutableList<Map<String, Any>> = ArrayList()
                list.forEach { item ->
                    result.add(storeData(item))
                }
                result.forEach { db.collection(COLLECTION_NAME).add(it) }
            } catch (e: Exception) {
                logger.error(e) { "Failed to send references to Firebase: ${e.message}" }
            }
        } else {
            logger.debug { "Firebase not available, skipping Firebase sync for references" }
        }

        return list
    }

    override fun getReferenceById(id: String): ReferenceResponse {
        logger.info { "Get details with reference id: $id" }
        val reference = repo.findById(id).orElseThrow { RuntimeException("Reference not found") }
        val response = convert(reference)

        if (db != null) {
            try {
                val data: ApiFuture<DocumentReference> = db.collection(COLLECTION_NAME).add(storeData(response))
                logger.info { "Data: ${data.get().id}" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to send reference to Firebase: ${e.message}" }
            }
        } else {
            logger.debug { "Firebase not available, skipping Firebase sync for reference" }
        }

        return response
    }

    fun convert(reference: Reference): ReferenceResponse {
        return ReferenceResponse(
            id = encoder.encode(reference.id),
            publicationYear = reference.metadata?.publicationYear ?: 0,
            doi = reference.metadata?.doi ?: "",
            title = reference.metadata?.title ?: "",
            authors = reference.metadata?.authors ?: "",
            journal = reference.metadata?.journal ?: "",
        )
    }

    fun storeData(item: ReferenceResponse): Map<String, Any> {
        val reference: MutableMap<String, Any> = HashMap()
        reference["id"] = item.id
        reference["publicationYear"] = item.publicationYear
        reference["doi"] = item.doi
        reference["title"] = item.title
        reference["authors"] = item.authors
        reference["journal"] = item.journal
        return reference
    }

}