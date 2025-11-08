package com.se1853_jv.controller

import com.se1853_jv.dto.request.UploadPdfRequest
import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.EncoderService
import com.se1853_jv.service.GrobidService
import com.se1853_jv.service.boundary.ReferenceService
import com.se1853_jv.service.boundary.PaperService
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("papers")
class PaperController(
    private val paperService: PaperService,
    private val referenceService: ReferenceService,
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

    @GetMapping("/references")
    fun getReferencesOfPaperResearch(@RequestParam("id") paperId: String): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to getCitationsOfPaperResearch controller. id: ${encoder.decode(paperId)}" }
        val id = encoder.decode(paperId)

        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get references of a paper successfully",
                referenceService.getReferencesByPaperId(id),
                LocalDateTime.now()
            )
        )
    }

    @PostMapping(
        "/pdf/upload",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun uploadPdfFile(@Valid @RequestBody request: UploadPdfRequest): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to uploadPdfFile controller" }
        paperService.createNewPaper(request)

        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Upload paper successfully",
                null,
                LocalDateTime.now()
            )
        )
    }

    @PostMapping(
        "/pdf/parse",
        produces = [MediaType.APPLICATION_JSON_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun extractDataFromPdfFile(@RequestBody file: MultipartFile): ResponseEntity<WrapperApiResponse> {
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

    @GetMapping("/all")
    fun getAllPapers(
        @RequestParam(value = "search", required = false) searchQuery: String?,
        @RequestParam index: Int,
        @RequestParam(value = "size", required = false) pageSize: Int,
        @RequestParam(value = "tagIds", required = false) tagIds: List<String>?
    ): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to getAllPapers with search: $searchQuery, tagIds: $tagIds" }
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get all papers successfully",
                paperService.getAllPapers(searchQuery, index, pageSize, tagIds),
                LocalDateTime.now()
            )
        )
    }

    @DeleteMapping("/{id}")
    fun deletePaper(@PathVariable("id") paperId: String): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to deletePaper with id: $paperId" }
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Delete paper successfully",
                paperService.deleteById(paperId),
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