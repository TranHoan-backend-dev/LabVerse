package com.se1853_jv.service.impl

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.se1853_jv.dto.response.TagResponse
import com.se1853_jv.model.Tag
import com.se1853_jv.repository.TagRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.TagService
import mu.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}
private const val COLLECTION_NAME: String = "tag"

@Service
class TagServiceImpl(
    private val repo: TagRepository,
    private val encoder: EncoderService,
    private val db: Firestore,
) : TagService {

    override fun getTagsByPaperId(id: String): List<TagResponse> {
        logger.info { "Get tags by paper with id: $id" }
        val tags = repo.findByPaperIdsContaining(id)
        val response = tags.map { convert(it) }

        sendDataToFirebase(response)

        return response
    }

    override fun getTheFiveMostPopularTag(): List<TagResponse> {
        logger.info { "Get the 5 most popular tags" }
//        val tags = repo.findAll(Sort.by(()))
        val tags = repo.findAll().subList(0, 4)
        val response = tags.map { convert(it) }

        sendDataToFirebase(response)

        return response
    }

    private fun convert(tag: Tag): TagResponse {
        return TagResponse(
            id = encoder.encode(tag.id!!),
            name = tag.name ?: "",
            numberOfPaperConnectingToTag = tag.numberOfPaperConnectingToTag ?: 0
        )
    }

    private fun storeData(tag: TagResponse): MutableMap<String, Any> {
        val response: MutableMap<String, Any> = HashMap()
        response["id"] = tag.id
        response["name"] = tag.name
        return response
    }

    private fun sendDataToFirebase(response: List<TagResponse>) {
        val dataList: MutableList<MutableMap<String, Any>> = ArrayList()
        response.forEach { item ->
            val result = storeData(item)
            dataList.add(result)
        }
        dataList.forEach { item -> db.collection(COLLECTION_NAME).add(item) }
    }
}