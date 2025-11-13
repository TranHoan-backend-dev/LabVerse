package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.response.CollectionProgressStatisticsResponse;
import com.se1853_jv.readingservice.dto.response.PaperProgressResponse;
import com.se1853_jv.readingservice.dto.response.TeamMemberProgressResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.ProgressTrackingService;
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
@RequestMapping("/progress")
@RequiredArgsConstructor
@Tag(name = "Progress Tracking", description = "API endpoints for tracking reading progress and team statistics")
public class ProgressTrackingController {

    private final ProgressTrackingService progressTrackingService;

    @GetMapping("/collection/{collectionId}")
    @Operation(summary = "Get collection progress statistics",
            description = "Get aggregated progress statistics for a collection. This is the main dashboard endpoint for PIs to view team progress. Collection ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded collection ID")
    })
    public ResponseEntity<WrapperApiResponse<CollectionProgressStatisticsResponse>> getCollectionProgress(
            @Parameter(description = "Encoded Collection ID", required = true)
            @PathVariable String collectionId) {
        String decodedCollectionId = IdEncoder.decodeString(collectionId);
        CollectionProgressStatisticsResponse response =
                progressTrackingService.getCollectionProgressStatistics(decodedCollectionId);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping("/collection/{collectionId}/member/{userId}")
    @Operation(summary = "Get team member progress in collection",
            description = "Get progress statistics for a specific team member in a collection. IDs should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team member progress retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<TeamMemberProgressResponse>> getTeamMemberProgress(
            @Parameter(description = "Encoded Collection ID", required = true)
            @PathVariable String collectionId,
            @Parameter(description = "Encoded User ID", required = true)
            @PathVariable String userId) {
        String decodedCollectionId = IdEncoder.decodeString(collectionId);
        String decodedUserId = IdEncoder.decodeString(userId);
        TeamMemberProgressResponse response =
                progressTrackingService.getTeamMemberProgress(decodedCollectionId, decodedUserId);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping("/collection/{collectionId}/paper/{paperId}")
    @Operation(summary = "Get paper progress in collection",
            description = "Get progress statistics for a specific paper in a collection. Shows how all team members are progressing with this paper. IDs should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paper progress retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<PaperProgressResponse>> getPaperProgress(
            @Parameter(description = "Encoded Collection ID", required = true)
            @PathVariable String collectionId,
            @Parameter(description = "Encoded Paper ID", required = true)
            @PathVariable String paperId) {
        String decodedCollectionId = IdEncoder.decodeString(collectionId);
        String decodedPaperId = IdEncoder.decodeString(paperId);
        PaperProgressResponse response =
                progressTrackingService.getPaperProgress(decodedCollectionId, decodedPaperId);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }
}








