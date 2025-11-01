package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.request.HighlightRequest;
import com.se1853_jv.readingservice.dto.response.HighlightResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.HighlightService;
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
@RequestMapping("/highlights")
@RequiredArgsConstructor
@Tag(name = "Highlight", description = "API endpoints for managing highlights")
public class HighlightController {

    private final HighlightService highlightService;

    @PostMapping
    @Operation(summary = "Add a highlight", 
               description = "Create a new highlight and link it to a reading workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highlight created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<WrapperApiResponse<HighlightResponse>> addHighlight(
            @Valid @RequestBody HighlightRequest request) {
        HighlightResponse response = highlightService.addHighlight(request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get highlights for a paper", 
               description = "Get all highlights for a specific paper and user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highlights retrieved successfully")
    })
    public ResponseEntity<WrapperApiResponse<List<HighlightResponse>>> getHighlights(
            @Parameter(description = "Collection ID", required = true) @RequestParam String collectionId,
            @Parameter(description = "Paper ID", required = true) @RequestParam String paperId,
            @Parameter(description = "User ID", required = true) @RequestParam String userId) {
        List<HighlightResponse> highlights = highlightService.getHighlights(collectionId, paperId, userId);
        return ResponseEntity.ok(WrapperApiResponse.success(highlights));
    }

    @DeleteMapping("/{highlightId}")
    @Operation(summary = "Delete a highlight", 
               description = "Delete a highlight and its workflow mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highlight deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Highlight not found")
    })
    public ResponseEntity<WrapperApiResponse<String>> deleteHighlight(
            @Parameter(description = "Highlight ID", required = true) @PathVariable UUID highlightId) {
        highlightService.deleteHighlight(highlightId);
        return ResponseEntity.ok(WrapperApiResponse.success("Highlight deleted successfully"));
    }
}

