package com.se1853_jv.controller

import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.boundary.CitationService
import com.se1853_jv.service.boundary.PaperService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("papers")
class PaperController(
    private val paperService: PaperService,
    private val citationService: CitationService,
    private val encoder: EncoderService
) {
    @GetMapping("/details")
    fun getDetails(@RequestParam("id") data: String): ResponseEntity<WrapperApiResponse> {
        val id = encoder.decode(data)

        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get paper details successfully",
                paperService.getPaperDetails(id),
                LocalDateTime.now()
            )
        )
    }

    @GetMapping("/citation")
    fun getCitationsOfPaperResearch(@RequestParam("id") paperId: String): ResponseEntity<WrapperApiResponse> {
        val id = encoder.decode(paperId)
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get citations of a paper successfully",
                citationService.getCitationsByPaperId(id),
                LocalDateTime.now()
            )
        )
    }
}