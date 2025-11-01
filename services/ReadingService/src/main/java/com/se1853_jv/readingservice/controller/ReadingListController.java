package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.request.ReadingListCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdatePapersRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdateUsersRequest;
import com.se1853_jv.readingservice.dto.response.ReadingListResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.ReadingListService;
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
@RequestMapping("/reading-lists")
@RequiredArgsConstructor
@Tag(name = "Reading List", description = "API endpoints for managing reading lists")
public class ReadingListController {

    private final ReadingListService readingListService;

    @PostMapping
    @Operation(summary = "Create a reading list", 
               description = "Create a new reading list (personal or group) with users and papers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading list created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<WrapperApiResponse<ReadingListResponse>> createReadingList(
            @Valid @RequestBody ReadingListCreateRequest request) {
        ReadingListResponse response = readingListService.createReadingList(request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reading lists by user", 
               description = "Get all reading lists that contain the specified user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading lists retrieved successfully")
    })
    public ResponseEntity<WrapperApiResponse<List<ReadingListResponse>>> getReadingListsByUser(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        List<ReadingListResponse> lists = readingListService.getReadingListsByUser(userId);
        return ResponseEntity.ok(WrapperApiResponse.success(lists));
    }

    @PutMapping("/{listId}/papers")
    @Operation(summary = "Add or remove papers from reading list", 
               description = "Update papers in a reading list. Action: 'add' or 'remove'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Papers updated successfully"),
            @ApiResponse(responseCode = "404", description = "Reading list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid action")
    })
    public ResponseEntity<WrapperApiResponse<ReadingListResponse>> updatePapers(
            @Parameter(description = "Reading List ID", required = true) @PathVariable UUID listId,
            @Valid @RequestBody ReadingListUpdatePapersRequest request) {
        ReadingListResponse response = readingListService.updatePapers(listId, request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @PutMapping("/{listId}/users")
    @Operation(summary = "Add or remove users from reading list", 
               description = "Update users in a reading list. Action: 'add' or 'remove'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users updated successfully"),
            @ApiResponse(responseCode = "404", description = "Reading list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid action")
    })
    public ResponseEntity<WrapperApiResponse<ReadingListResponse>> updateUsers(
            @Parameter(description = "Reading List ID", required = true) @PathVariable UUID listId,
            @Valid @RequestBody ReadingListUpdateUsersRequest request) {
        ReadingListResponse response = readingListService.updateUsers(listId, request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @DeleteMapping("/{listId}")
    @Operation(summary = "Delete a reading list", 
               description = "Delete a reading list by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading list deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Reading list not found")
    })
    public ResponseEntity<WrapperApiResponse<String>> deleteReadingList(
            @Parameter(description = "Reading List ID", required = true) @PathVariable UUID listId) {
        readingListService.deleteReadingList(listId);
        return ResponseEntity.ok(WrapperApiResponse.success("Reading list deleted successfully"));
    }
}

