package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.request.HighlightRequest;
import com.se1853_jv.readingservice.dto.response.HighlightResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.HighlightService;
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
@RequestMapping("/highlights")
@RequiredArgsConstructor
@Tag(name = "Highlight", description = "API endpoints for managing highlights")
public class HighlightController {

    private final HighlightService highlightService;

    @PostMapping
    @Operation(summary = "Add a highlight", 
               description = "Create a new highlight and link it to a reading workflow. IDs should be encoded in request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highlight created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<HighlightResponse>> addHighlight(
            @Valid @RequestBody HighlightRequest request) {
        // Decode IDs from request
        request.setCollectionId(IdEncoder.decodeString(request.getCollectionId()));
        request.setPaperId(IdEncoder.decodeString(request.getPaperId()));
        request.setUsersid(IdEncoder.decodeString(request.getUsersid()));
        HighlightResponse response = highlightService.addHighlight(request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get highlights for a paper", 
               description = "Get all highlights for a specific paper and user. IDs should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highlights retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<List<HighlightResponse>>> getHighlights(
            @Parameter(description = "Encoded Collection ID", required = true) @RequestParam String collectionId,
            @Parameter(description = "Encoded Paper ID", required = true) @RequestParam String paperId,
            @Parameter(description = "Encoded User ID", required = true) @RequestParam String userId) {
        String decodedCollectionId = IdEncoder.decodeString(collectionId);
        String decodedPaperId = IdEncoder.decodeString(paperId);
        String decodedUsersid = IdEncoder.decodeString(userId);
        List<HighlightResponse> highlights = highlightService.getHighlights(decodedCollectionId, decodedPaperId, decodedUsersid);
        return ResponseEntity.ok(WrapperApiResponse.success(highlights));
    }

    @DeleteMapping("/{highlightId}")
    @Operation(summary = "Delete a highlight", 
               description = "Delete a highlight and its workflow mapping. Highlight ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Highlight deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Highlight not found"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded highlight ID")
    })
    public ResponseEntity<WrapperApiResponse<String>> deleteHighlight(
            @Parameter(description = "Encoded Highlight ID", required = true) @PathVariable String highlightId) {
        String decodedHighlightId = IdEncoder.decodeString(highlightId);
        highlightService.deleteHighlight(decodedHighlightId);
        return ResponseEntity.ok(WrapperApiResponse.success("Highlight deleted successfully"));
    }
}

