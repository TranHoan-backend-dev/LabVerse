package com.se1853_jv.controller

import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.GrobidService
import com.se1853_jv.service.boundary.CitationService
import com.se1853_jv.service.boundary.PaperService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("papers")
class PaperController(
    private val paperService: PaperService,
    private val citationService: CitationService,
    private val encoder: EncoderService,
    private val grobid: GrobidService
) {
    @GetMapping("/details")
    fun getDetails(@RequestParam("id") data: String): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to getDetails controller. id: ${encoder.decode(data)}" }
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
        logger.info { "Request to getCitationsOfPaperResearch controller. id: ${encoder.decode(paperId)}" }
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

    @PostMapping("/pdf/parse", produces = ["application/json"], consumes = ["multipart/form-data"])
    fun extractDataFromPdfFile(@RequestParam("file") file: MultipartFile): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to extractDataFromPdfFile controller" }
        val temp: File = File.createTempFile("pdf_", ".pdf")
        file.transferTo(temp)

        val metadata: MutableList<MutableMap<String, String>> = grobid.parsePdf(temp)
        temp.delete()

        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Parse citations of a paper successfully",
                metadata,
                LocalDateTime.now()
            )
        )
    }
}