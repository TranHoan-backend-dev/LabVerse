package com.se1853_jv.controller

import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.CitationService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("citations")
class CitationController(
    private val service: CitationService,
    private val encoder: EncoderService
) {

    @GetMapping("/{id}")
    fun getDetails(@PathVariable id: String): ResponseEntity<WrapperApiResponse> {
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get citations of a paper successfully",
                service.getCitationById(encoder.decode(id)),
                LocalDateTime.now()
            )
        )
    }

    @RequestMapping(value = ["/health"], method = [RequestMethod.HEAD])
    fun health(): ResponseEntity<Any> {
        logger.info { "HEALTHY" }
        return ResponseEntity.ok().build()
    }
}