package com.se1853_jv.controller;

import com.se1853_jv.config.JwtConfig;
import com.se1853_jv.dto.request.CreateHighlightRequest;
import com.se1853_jv.dto.request.CreateNoteRequest;
import com.se1853_jv.dto.response.ExportAnnotationsResponse;
import com.se1853_jv.dto.response.HighlightResponse;
import com.se1853_jv.dto.response.NoteResponse;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.service.AnnotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
@Slf4j
@RestController
@RequestMapping("annotations")
@RequiredArgsConstructor
public class AnnotationController {
    
    private final AnnotationService annotationService;
    private final JwtConfig jwtConfig;
    
    /**
     * Create a new note
     * POST /v1/api/annotations/notes
     */
    @PostMapping("/notes")
    public ResponseEntity<WrapperApiResponse<NoteResponse>> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Request to create note for paper={}", request.getPaperId());
        
        String userId = extractUserIdFromToken(authHeader);
        NoteResponse note = annotationService.createNote(userId, request);
        
        return ResponseEntity.ok(
                WrapperApiResponse.<NoteResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Note created successfully")
                        .data(note)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Create a new highlight
     * POST /v1/api/annotations/highlights
     */
    @PostMapping("/highlights")
    public ResponseEntity<WrapperApiResponse<HighlightResponse>> createHighlight(
            @Valid @RequestBody CreateHighlightRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Request to create highlight for paper={}", request.getPaperId());
        
        String userId = extractUserIdFromToken(authHeader);
        HighlightResponse highlight = annotationService.createHighlight(userId, request);
        
        return ResponseEntity.ok(
                WrapperApiResponse.<HighlightResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Highlight created successfully")
                        .data(highlight)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Get all notes for a paper
     * GET /v1/api/annotations/notes?paperId=...&collectionId=...&userId=...
     */
    @GetMapping("/notes")
    public ResponseEntity<WrapperApiResponse<List<NoteResponse>>> getNotes(
            @RequestParam("paperId") String paperId,
            @RequestParam(value = "collectionId", required = false) String collectionId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Request to get notes for paper={}, collection={}, user={}", paperId, collectionId, userId);
        
        // If userId not provided, try to extract from token
        String finalUserId = userId != null ? userId : (authHeader != null ? extractUserIdFromToken(authHeader) : null);
        
        List<NoteResponse> notes = annotationService.getNotes(paperId, collectionId, finalUserId);
        
        return ResponseEntity.ok(
                WrapperApiResponse.<List<NoteResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Get notes successfully")
                        .data(notes)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Get all highlights for a paper
     * GET /v1/api/annotations/highlights?paperId=...&collectionId=...&userId=...
     */
    @GetMapping("/highlights")
    public ResponseEntity<WrapperApiResponse<List<HighlightResponse>>> getHighlights(
            @RequestParam("paperId") String paperId,
            @RequestParam(value = "collectionId", required = false) String collectionId,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Request to get highlights for paper={}, collection={}, user={}", paperId, collectionId, userId);
        
        String finalUserId = userId != null ? userId : (authHeader != null ? extractUserIdFromToken(authHeader) : null);
        
        List<HighlightResponse> highlights = annotationService.getHighlights(paperId, collectionId, finalUserId);
        
        return ResponseEntity.ok(
                WrapperApiResponse.<List<HighlightResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Get highlights successfully")
                        .data(highlights)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Delete a note
     * DELETE /v1/api/annotations/notes/{id}?paperId=...&collectionId=...
     */
    @DeleteMapping("/notes/{id}")
    public ResponseEntity<WrapperApiResponse<Void>> deleteNote(
            @PathVariable("id") String noteId,
            @RequestParam("paperId") String paperId,
            @RequestParam("collectionId") String collectionId,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Request to delete note={}", noteId);
        
        String userId = extractUserIdFromToken(authHeader);
        annotationService.deleteNote(userId, noteId, paperId, collectionId);
        
        return ResponseEntity.ok(
                WrapperApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Note deleted successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Delete a highlight
     * DELETE /v1/api/annotations/highlights/{id}?paperId=...&collectionId=...
     */
    @DeleteMapping("/highlights/{id}")
    public ResponseEntity<WrapperApiResponse<Void>> deleteHighlight(
            @PathVariable("id") String highlightId,
            @RequestParam("paperId") String paperId,
            @RequestParam("collectionId") String collectionId,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Request to delete highlight={}", highlightId);
        
        String userId = extractUserIdFromToken(authHeader);
        annotationService.deleteHighlight(userId, highlightId, paperId, collectionId);
        
        return ResponseEntity.ok(
                WrapperApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Highlight deleted successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Export all annotations for a paper in a collection
     * GET /v1/api/annotations/export?paperId=...&collectionId=...
     */
    @GetMapping("/export")
    public ResponseEntity<WrapperApiResponse<ExportAnnotationsResponse>> exportAnnotations(
            @RequestParam("paperId") String paperId,
            @RequestParam("collectionId") String collectionId,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Request to export annotations for paper={}, collection={}", paperId, collectionId);
        
        String userId = extractUserIdFromToken(authHeader);
        ExportAnnotationsResponse exportData = annotationService.exportAnnotations(paperId, collectionId, userId);
        
        return ResponseEntity.ok(
                WrapperApiResponse.<ExportAnnotationsResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Annotations exported successfully")
                        .data(exportData)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Import annotations from exported data
     * POST /v1/api/annotations/import?paperId=...&collectionId=...
     */
    @PostMapping("/import")
    public ResponseEntity<WrapperApiResponse<Void>> importAnnotations(
            @RequestParam("paperId") String paperId,
            @RequestParam("collectionId") String collectionId,
            @RequestBody ExportAnnotationsResponse importData,
            @RequestHeader("Authorization") String authHeader
    ) {
        log.info("Request to import annotations for paper={}, collection={}", paperId, collectionId);
        
        String userId = extractUserIdFromToken(authHeader);
        
        // Validate paperId and collectionId match
        if (!importData.getPaperId().equals(paperId) || !importData.getCollectionId().equals(collectionId)) {
            throw new IllegalArgumentException("Paper ID or Collection ID mismatch");
        }
        
        // Import notes
        if (importData.getNotes() != null) {
            for (NoteResponse noteResponse : importData.getNotes()) {
                CreateNoteRequest noteRequest = new CreateNoteRequest();
                noteRequest.setPaperId(paperId);
                noteRequest.setCollectionId(collectionId);
                noteRequest.setContent(noteResponse.getContent());
                noteRequest.setCoordinationX(noteResponse.getCoordinationX().intValue());
                noteRequest.setCoordinationY(noteResponse.getCoordinationY().intValue());
                noteRequest.setPageNumber(noteResponse.getPageNumber());
                
                annotationService.createNote(userId, noteRequest);
            }
        }
        
        // Import highlights
        if (importData.getHighlights() != null) {
            for (HighlightResponse highlightResponse : importData.getHighlights()) {
                CreateHighlightRequest highlightRequest = new CreateHighlightRequest();
                highlightRequest.setPaperId(paperId);
                highlightRequest.setCollectionId(collectionId);
                highlightRequest.setColor(highlightResponse.getColor());
                highlightRequest.setCoordinationX(highlightResponse.getCoordinationX().intValue());
                highlightRequest.setCoordinationY(highlightResponse.getCoordinationY().intValue());
                highlightRequest.setPageNumber(highlightResponse.getPageNumber());
                
                annotationService.createHighlight(userId, highlightRequest);
            }
        }
        
        return ResponseEntity.ok(
                WrapperApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Annotations imported successfully")
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
    
    /**
     * Helper method to extract userId from JWT token
     * Token format: "Bearer {jwt_token}"
     */
    private String extractUserIdFromToken(String authHeader) {
        if (!authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header format");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        return jwtConfig.extractUserIdFromToken(token);
    }
}

