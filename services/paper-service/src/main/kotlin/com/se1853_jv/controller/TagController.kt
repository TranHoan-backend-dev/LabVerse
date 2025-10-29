package com.se1853_jv.controller

import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.TagService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("tags")
class TagController(
    private val service: TagService,
    private val encoder: EncoderService
) {

    @GetMapping("/paper/{id}")
    fun getTagsByPaper(@PathVariable id: String): ResponseEntity<WrapperApiResponse> {
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get tags successfully",
                service.getTagsByPaperId(encoder.decode(id)),
                LocalDateTime.now()
            )
        )
    }

    @GetMapping("/")
    fun getTheFiveMostPopularTags(): ResponseEntity<WrapperApiResponse> {
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get tags successfully",
                service.getTheFiveMostPopularTag(),
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