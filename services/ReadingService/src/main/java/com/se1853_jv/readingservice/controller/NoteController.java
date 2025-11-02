package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.request.NoteRequest;
import com.se1853_jv.readingservice.dto.request.NoteUpdateRequest;
import com.se1853_jv.readingservice.dto.response.NoteResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.NoteService;
import com.se1853_jv.readingservice.util.IdEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "Note", description = "API endpoints for managing notes")
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @Operation(summary = "Add a note", 
               description = "Create a new note and link it to a reading workflow. IDs should be encoded in request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<NoteResponse>> addNote(
            @Valid @RequestBody NoteRequest request) {
        // Decode IDs from request
        request.setCollectionId(IdEncoder.decodeString(request.getCollectionId()));
        request.setPaperId(IdEncoder.decodeString(request.getPaperId()));
        request.setUserId(IdEncoder.decodeString(request.getUserId()));
        NoteResponse response = noteService.addNote(request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get notes for a paper", 
               description = "Get all notes for a specific paper and user. IDs should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<List<NoteResponse>>> getNotes(
            @Parameter(description = "Encoded Collection ID", required = true) @RequestParam String collectionId,
            @Parameter(description = "Encoded Paper ID", required = true) @RequestParam String paperId,
            @Parameter(description = "Encoded User ID", required = true) @RequestParam String userId) {
        String decodedCollectionId = IdEncoder.decodeString(collectionId);
        String decodedPaperId = IdEncoder.decodeString(paperId);
        String decodedUserId = IdEncoder.decodeString(userId);
        List<NoteResponse> notes = noteService.getNotes(decodedCollectionId, decodedPaperId, decodedUserId);
        return ResponseEntity.ok(WrapperApiResponse.success(notes));
    }

    @PutMapping("/{noteId}")
    @Operation(summary = "Update a note", 
               description = "Update note content. Validates ownership via workflow. Note ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note updated successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "403", description = "Permission denied"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded note ID")
    })
    public ResponseEntity<WrapperApiResponse<NoteResponse>> updateNote(
            @Parameter(description = "Encoded Note ID", required = true) @PathVariable String noteId,
            @Valid @RequestBody NoteUpdateRequest request) {
        java.util.UUID decodedNoteId = IdEncoder.decodeUuid(noteId);
        NoteResponse response = noteService.updateNote(decodedNoteId, request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete a note", 
               description = "Delete a note and its workflow mapping. Note ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded note ID")
    })
    public ResponseEntity<WrapperApiResponse<String>> deleteNote(
            @Parameter(description = "Encoded Note ID", required = true) @PathVariable String noteId) {
        java.util.UUID decodedNoteId = IdEncoder.decodeUuid(noteId);
        noteService.deleteNote(decodedNoteId);
        return ResponseEntity.ok(WrapperApiResponse.success("Note deleted successfully"));
    }
}

