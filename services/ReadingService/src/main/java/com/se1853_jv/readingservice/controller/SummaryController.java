package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.response.AnnotationsResponse;
import com.se1853_jv.readingservice.dto.response.ReadingSummaryResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.SummaryService;
import com.se1853_jv.readingservice.util.IdEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/summary")
@RequiredArgsConstructor
@Tag(name = "Summary & Analytics", description = "API endpoints for reading statistics and annotations")
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get reading statistics for user", 
               description = "Get counts of unread, reading, and finished workflows for a user. User ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded user ID")
    })
    public ResponseEntity<WrapperApiResponse<ReadingSummaryResponse>> getReadingSummary(
            @Parameter(description = "Encoded User ID", required = true) @PathVariable String userId) {
        String decodedUserId = IdEncoder.decodeString(userId);
        ReadingSummaryResponse response = summaryService.getReadingSummary(decodedUserId);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping("/annotations/{paperId}/user/{userId}")
    @Operation(summary = "Get all annotations for a paper", 
               description = "Get all notes and highlights for a specific paper and user. IDs should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annotations retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<AnnotationsResponse>> getAnnotations(
            @Parameter(description = "Encoded Paper ID", required = true) @PathVariable String paperId,
            @Parameter(description = "Encoded User ID", required = true) @PathVariable String userId) {
        String decodedPaperId = IdEncoder.decodeString(paperId);
        String decodedUserId = IdEncoder.decodeString(userId);
        AnnotationsResponse response = summaryService.getAnnotations(decodedPaperId, decodedUserId);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }
}

