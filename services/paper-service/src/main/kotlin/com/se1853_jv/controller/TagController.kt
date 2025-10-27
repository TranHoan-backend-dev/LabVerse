package com.se1853_jv.controller

import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.TagService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

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
}