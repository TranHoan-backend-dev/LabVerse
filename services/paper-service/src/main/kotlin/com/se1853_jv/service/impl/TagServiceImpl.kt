package com.se1853_jv.service.impl

import com.se1853_jv.dto.response.TagResponse
import com.se1853_jv.model.Tag
import com.se1853_jv.repository.TagRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.TagService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class TagServiceImpl(
    private val repo: TagRepository,
    private val encoder: EncoderService
) : TagService {

    override fun getTagsByPaperId(id: String): List<TagResponse> {
        logger.info { "Get tags by paper with id: $id" }
        val tags = repo.findByPaperIdsContaining(id)
        return tags.map { convert(it) }
    }

    private fun convert(tag: Tag): TagResponse {
        return TagResponse(
            id = encoder.encode(tag.id!!),
            name = tag.name ?: ""
        )
    }
}