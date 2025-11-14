package com.se1853_jv.controller

import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.repository.PaperRepository
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/internal/api/papers")
class InternalPaperController(
    private val paperRepository: PaperRepository
) {
    @GetMapping("/statistics")
    fun getStatistics(): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to getPaperStatistics" }
        
        val totalPapers = paperRepository.count()
        
        // Note: Paper model doesn't have createdAt field
        // For now, we return 0 for papersThisMonth
        // TODO: Add createdAt field to Paper model to track creation date
        val papersThisMonth = 0L
        
        val statistics = mapOf(
            "totalPapers" to totalPapers,
            "papersThisMonth" to papersThisMonth
        )
        
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get paper statistics successfully",
                statistics,
                LocalDateTime.now()
            )
        )
    }
}

