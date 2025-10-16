package com.se1853_jv.service.impl

import com.se1853_jv.dto.response.CitationResponse
import com.se1853_jv.model.Citation
import com.se1853_jv.repository.CitationRepository
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.CitationService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class CitationServiceImpl(
    private val repo: CitationRepository,
    private val encoder: EncoderService
) : CitationService {
    override fun getCitationsByPaperId(id: String): List<CitationResponse> {
        logger.info { "Get citations with paper id $id" }
        return repo.findByPaperIdsContaining(id).map { convert(it) }
    }

    override fun getCitationById(id: String): CitationResponse {
        logger.info { "Get details with citation id: $id" }
        return convert(repo.findById(id).orElseThrow { RuntimeException("Citation not found") })
    }

    fun convert(citation: Citation): CitationResponse {
        return CitationResponse(
            id = encoder.encode(citation.id),
            publicationYear = citation.publicationYear!!,
            doi = citation.doi!!,
            title = citation.title!!,
            authors = citation.authors!!,
            journal = citation.journal!!,
        )
    }
}