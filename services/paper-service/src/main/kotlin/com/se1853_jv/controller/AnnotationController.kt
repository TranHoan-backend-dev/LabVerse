package com.se1853_jv.controller

import com.se1853_jv.dto.request.CreateHighlightRequest
import com.se1853_jv.dto.request.CreateNoteRequest
import com.se1853_jv.dto.response.HighlightResponse
import com.se1853_jv.dto.response.NoteResponse
import com.se1853_jv.config.JwtConfig
import com.se1853_jv.dto.response.WrapperApiResponse
import com.se1853_jv.service.boundary.AnnotationService
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * Controller for managing annotations (notes and highlights) on PDF papers
 * Endpoints:
 * - POST /annotations/notes - Create a note
 * - POST /annotations/highlights - Create a highlight
 * - GET /annotations/notes - Get notes for a paper
 * - GET /annotations/highlights - Get highlights for a paper
 * - DELETE /annotations/notes/{id} - Delete a note
 * - DELETE /annotations/highlights/{id} - Delete a highlight
 */
@RestController
@RequestMapping("annotations")
class AnnotationController(
    private val annotationService: AnnotationService,
    private val jwtConfig: JwtConfig
) {

    /**
     * Create a new note
     * POST /v1/api/annotations/notes
     */
    @PostMapping("/notes")
    fun createNote(
        @Valid @RequestBody request: CreateNoteRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to create note for paper=${request.paperId}" }
        
        // TODO: Extract userId from JWT token
        // For now, using a placeholder - cần tích hợp với AccountService JWT
        val userId = extractUserIdFromToken(authHeader)
        
        val note = annotationService.createNote(userId, request)
        
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Note created successfully",
                note,
                LocalDateTime.now()
            )
        )
    }

    /**
     * Create a new highlight
     * POST /v1/api/annotations/highlights
     */
    @PostMapping("/highlights")
    fun createHighlight(
        @Valid @RequestBody request: CreateHighlightRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to create highlight for paper=${request.paperId}" }
        
        val userId = extractUserIdFromToken(authHeader)
        
        val highlight = annotationService.createHighlight(userId, request)
        
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Highlight created successfully",
                highlight,
                LocalDateTime.now()
            )
        )
    }

    /**
     * Get all notes for a paper
     * GET /v1/api/annotations/notes?paperId=...&collectionId=...&userId=...
     */
    @GetMapping("/notes")
    fun getNotes(
        @RequestParam("paperId") paperId: String,
        @RequestParam(value = "collectionId", required = false) collectionId: String?,
        @RequestParam(value = "userId", required = false) userId: String?,
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to get notes for paper=$paperId, collection=$collectionId" }
        
        // If userId not provided, try to extract from token
        val finalUserId = userId ?: authHeader?.let { extractUserIdFromToken(it) }
        
        val notes = annotationService.getNotes(paperId, collectionId, finalUserId)
        
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get notes successfully",
                notes,
                LocalDateTime.now()
            )
        )
    }

    /**
     * Get all highlights for a paper
     * GET /v1/api/annotations/highlights?paperId=...&collectionId=...&userId=...
     */
    @GetMapping("/highlights")
    fun getHighlights(
        @RequestParam("paperId") paperId: String,
        @RequestParam(value = "collectionId", required = false) collectionId: String?,
        @RequestParam(value = "userId", required = false) userId: String?,
        @RequestHeader(value = "Authorization", required = false) authHeader: String?
    ): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to get highlights for paper=$paperId, collection=$collectionId" }
        
        val finalUserId = userId ?: authHeader?.let { extractUserIdFromToken(it) }
        
        val highlights = annotationService.getHighlights(paperId, collectionId, finalUserId)
        
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Get highlights successfully",
                highlights,
                LocalDateTime.now()
            )
        )
    }

    /**
     * Delete a note
     * DELETE /v1/api/annotations/notes/{id}?paperId=...&collectionId=...
     */
    @DeleteMapping("/notes/{id}")
    fun deleteNote(
        @PathVariable("id") noteId: String,
        @RequestParam("paperId") paperId: String,
        @RequestParam("collectionId") collectionId: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to delete note=$noteId" }
        
        val userId = extractUserIdFromToken(authHeader)
        
        annotationService.deleteNote(userId, noteId, paperId, collectionId)
        
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Note deleted successfully",
                null,
                LocalDateTime.now()
            )
        )
    }

    /**
     * Delete a highlight
     * DELETE /v1/api/annotations/highlights/{id}?paperId=...&collectionId=...
     */
    @DeleteMapping("/highlights/{id}")
    fun deleteHighlight(
        @PathVariable("id") highlightId: String,
        @RequestParam("paperId") paperId: String,
        @RequestParam("collectionId") collectionId: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<WrapperApiResponse> {
        logger.info { "Request to delete highlight=$highlightId" }
        
        val userId = extractUserIdFromToken(authHeader)
        
        annotationService.deleteHighlight(userId, highlightId, paperId, collectionId)
        
        return ResponseEntity.ok(
            WrapperApiResponse(
                HttpStatus.OK.value(),
                "Highlight deleted successfully",
                null,
                LocalDateTime.now()
            )
        )
    }

    /**
     * Helper method to extract userId from JWT token
     * Token format: "Bearer {jwt_token}"
     */
    private fun extractUserIdFromToken(authHeader: String): String {
        if (!authHeader.startsWith("Bearer ")) {
            throw IllegalArgumentException("Invalid authorization header format")
        }
        
        val token = authHeader.substring(7) // Remove "Bearer " prefix
        
        return jwtConfig.extractUserIdFromToken(token)
    }
}

