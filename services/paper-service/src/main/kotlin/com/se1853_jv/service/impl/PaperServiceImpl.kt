package com.se1853_jv.service.impl

import com.se1853_jv.dto.response.PaperResponse
import com.se1853_jv.model.Paper
import com.se1853_jv.repository.PaperRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.PaperService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class PaperServiceImpl(
    private val repo: PaperRepository,
    private val encoder: EncoderService
) : PaperService {
    override fun getPaperDetails(paperId: String): PaperResponse {
        logger.info { "Fetching details for paperId=$paperId" }
        val paper = repo.findById(paperId).orElseThrow { IllegalArgumentException("Paper not found") }
        return convert(paper)
    }

    private fun convert(paper: Paper): PaperResponse {
        return PaperResponse(
            id = encoder.encode(paper.id!!),
            dataUrl = paper.dataUrl!!,
            keywords = paper.keywords,
            title = paper.title!!,
            authors = paper.title,
            journal = paper.journal!!,
            publicationYear = paper.publicationYear!!,
            doi = paper.doi!!,
        )
    }
}