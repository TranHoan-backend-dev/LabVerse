package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.request.NoteRequest;
import com.se1853_jv.readingservice.dto.request.NoteUpdateRequest;
import com.se1853_jv.readingservice.dto.response.NoteResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.NoteService;
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
import java.util.UUID;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "Note", description = "API endpoints for managing notes")
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @Operation(summary = "Add a note", 
               description = "Create a new note and link it to a reading workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<WrapperApiResponse<NoteResponse>> addNote(
            @Valid @RequestBody NoteRequest request) {
        NoteResponse response = noteService.addNote(request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get notes for a paper", 
               description = "Get all notes for a specific paper and user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully")
    })
    public ResponseEntity<WrapperApiResponse<List<NoteResponse>>> getNotes(
            @Parameter(description = "Collection ID", required = true) @RequestParam String collectionId,
            @Parameter(description = "Paper ID", required = true) @RequestParam String paperId,
            @Parameter(description = "User ID", required = true) @RequestParam String userId) {
        List<NoteResponse> notes = noteService.getNotes(collectionId, paperId, userId);
        return ResponseEntity.ok(WrapperApiResponse.success(notes));
    }

    @PutMapping("/{noteId}")
    @Operation(summary = "Update a note", 
               description = "Update note content. Validates ownership via workflow.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note updated successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found"),
            @ApiResponse(responseCode = "403", description = "Permission denied")
    })
    public ResponseEntity<WrapperApiResponse<NoteResponse>> updateNote(
            @Parameter(description = "Note ID", required = true) @PathVariable UUID noteId,
            @Valid @RequestBody NoteUpdateRequest request) {
        NoteResponse response = noteService.updateNote(noteId, request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete a note", 
               description = "Delete a note and its workflow mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    public ResponseEntity<WrapperApiResponse<String>> deleteNote(
            @Parameter(description = "Note ID", required = true) @PathVariable UUID noteId) {
        noteService.deleteNote(noteId);
        return ResponseEntity.ok(WrapperApiResponse.success("Note deleted successfully"));
    }
}

