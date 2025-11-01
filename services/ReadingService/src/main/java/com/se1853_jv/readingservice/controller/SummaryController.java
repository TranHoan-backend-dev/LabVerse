package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.response.AnnotationsResponse;
import com.se1853_jv.readingservice.dto.response.ReadingSummaryResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.SummaryService;
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
               description = "Get counts of unread, reading, and finished workflows for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<WrapperApiResponse<ReadingSummaryResponse>> getReadingSummary(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        ReadingSummaryResponse response = summaryService.getReadingSummary(userId);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping("/annotations/{paperId}/user/{userId}")
    @Operation(summary = "Get all annotations for a paper", 
               description = "Get all notes and highlights for a specific paper and user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Annotations retrieved successfully")
    })
    public ResponseEntity<WrapperApiResponse<AnnotationsResponse>> getAnnotations(
            @Parameter(description = "Paper ID", required = true) @PathVariable String paperId,
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        AnnotationsResponse response = summaryService.getAnnotations(paperId, userId);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }
}

