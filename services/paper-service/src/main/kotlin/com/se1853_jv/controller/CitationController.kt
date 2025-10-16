package com.se1853_jv.controller

import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.CitationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

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
//                service.getCitationById(encoder.decode(id)),
                service.getCitationById(id),
                LocalDateTime.now()
            )
        )
    }
}