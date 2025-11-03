package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.request.ReadingWorkflowCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowDeleteRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowProgressRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowStatusRequest;
import com.se1853_jv.readingservice.dto.response.ReadingWorkflowResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.ReadingWorkflowService;
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
@RequestMapping("/workflows")
@RequiredArgsConstructor
@Tag(name = "Reading Workflow", description = "API endpoints for managing reading workflows")
public class ReadingWorkflowController {

    private final ReadingWorkflowService readingWorkflowService;

    @PostMapping
    @Operation(summary = "Create a new reading workflow", 
               description = "Start reading a paper. Checks if workflow exists, creates new with status='unread', lastPage=0, progress=0. IDs should be encoded in request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<ReadingWorkflowResponse>> createWorkflow(
            @Valid @RequestBody ReadingWorkflowCreateRequest request) {
        // Decode IDs from request if provided
        if (request.getCollectionId() != null) {
            request.setCollectionId(IdEncoder.decodeString(request.getCollectionId()));
        }
        if (request.getPaperId() != null) {
            request.setPaperId(IdEncoder.decodeString(request.getPaperId()));
        }
        if (request.getUsersid() != null) {
            request.setUsersid(IdEncoder.decodeString(request.getUsersid()));
        }
        ReadingWorkflowResponse response = readingWorkflowService.createWorkflow(request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get workflows by user", 
               description = "Get all reading workflows for a user, optionally filtered by status. User ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflows retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded user ID")
    })
    public ResponseEntity<WrapperApiResponse<List<ReadingWorkflowResponse>>> getWorkflowsByUser(
            @Parameter(description = "Encoded User ID", required = true) @PathVariable String userId,
            @Parameter(description = "Filter by status: unread, reading, or finished") 
            @RequestParam(required = false) String status) {
        String decodedUsersid = IdEncoder.decodeString(userId);
        List<ReadingWorkflowResponse> workflows = readingWorkflowService.getWorkflowsByUser(decodedUsersid, status);
        return ResponseEntity.ok(WrapperApiResponse.success(workflows));
    }

    @PutMapping("/progress")
    @Operation(summary = "Update reading progress", 
               description = "Update lastPage and progress. If progress >= 100, status is auto-updated to 'finished'. IDs should be encoded in request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Progress updated successfully"),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<String>> updateProgress(
            @Valid @RequestBody ReadingWorkflowProgressRequest request) {
        // Decode IDs from request
        request.setCollectionId(IdEncoder.decodeString(request.getCollectionId()));
        request.setPaperId(IdEncoder.decodeString(request.getPaperId()));
        request.setUsersid(IdEncoder.decodeString(request.getUsersid()));
        readingWorkflowService.updateProgress(request);
        return ResponseEntity.ok(WrapperApiResponse.success("Progress updated successfully"));
    }

    @PatchMapping("/status")
    @Operation(summary = "Update reading status", 
               description = "Update the status of a reading workflow. IDs should be encoded in request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<String>> updateStatus(
            @Valid @RequestBody ReadingWorkflowStatusRequest request) {
        // Decode IDs from request
        request.setCollectionId(IdEncoder.decodeString(request.getCollectionId()));
        request.setPaperId(IdEncoder.decodeString(request.getPaperId()));
        request.setUsersid(IdEncoder.decodeString(request.getUsersid()));
        readingWorkflowService.updateStatus(request);
        return ResponseEntity.ok(WrapperApiResponse.success("Status updated successfully"));
    }

    @DeleteMapping
    @Operation(summary = "Delete reading workflow", 
               description = "Delete a reading workflow and all associated notes/highlights mappings. IDs should be encoded in request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Workflow not found"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<String>> deleteWorkflow(
            @Valid @RequestBody ReadingWorkflowDeleteRequest request) {
        // Decode IDs from request
        request.setCollectionId(IdEncoder.decodeString(request.getCollectionId()));
        request.setPaperId(IdEncoder.decodeString(request.getPaperId()));
        request.setUsersid(IdEncoder.decodeString(request.getUsersid()));
        readingWorkflowService.deleteWorkflow(request);
        return ResponseEntity.ok(WrapperApiResponse.success("Workflow deleted successfully"));
    }
}
