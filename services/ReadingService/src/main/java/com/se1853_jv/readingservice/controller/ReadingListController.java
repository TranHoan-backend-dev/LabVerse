package com.se1853_jv.readingservice.controller;

import com.se1853_jv.readingservice.dto.request.ReadingListCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdatePapersRequest;
import com.se1853_jv.readingservice.dto.request.ReadingListUpdateUsersRequest;
import com.se1853_jv.readingservice.dto.response.ReadingListResponse;
import com.se1853_jv.readingservice.dto.response.WrapperApiResponse;
import com.se1853_jv.readingservice.service.ReadingListService;
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
@RequestMapping("/reading-lists")
@RequiredArgsConstructor
@Tag(name = "Reading List", description = "API endpoints for managing reading lists")
public class ReadingListController {

    private final ReadingListService readingListService;

    @PostMapping
    @Operation(summary = "Create a reading list", 
               description = "Create a new reading list (personal or group) with users and papers. IDs in lists should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading list created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<ReadingListResponse>> createReadingList(
            @Valid @RequestBody ReadingListCreateRequest request) {
        // Decode IDs in lists
        if (request.getUserIdsList() != null) {
            request.setUserIdsList(request.getUserIdsList().stream()
                    .map(IdEncoder::decodeString)
                    .collect(java.util.stream.Collectors.toList()));
        }
        if (request.getPaperIdsList() != null) {
            request.setPaperIdsList(request.getPaperIdsList().stream()
                    .map(IdEncoder::decodeString)
                    .collect(java.util.stream.Collectors.toList()));
        }
        ReadingListResponse response = readingListService.createReadingList(request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @GetMapping("/{listId}")
    @Operation(summary = "Get reading list by ID", 
               description = "Get a reading list by its ID. List ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Reading list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded list ID")
    })
    public ResponseEntity<WrapperApiResponse<ReadingListResponse>> getReadingListById(
            @Parameter(description = "Encoded Reading List ID", required = true) @PathVariable String listId) {
        String decodedListId = IdEncoder.decodeString(listId);
        ReadingListResponse list = readingListService.getReadingListById(decodedListId);
        return ResponseEntity.ok(WrapperApiResponse.success(list));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reading lists by user", 
               description = "Get all reading lists that contain the specified user. User ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading lists retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded user ID")
    })
    public ResponseEntity<WrapperApiResponse<List<ReadingListResponse>>> getReadingListsByUser(
            @Parameter(description = "Encoded User ID", required = true) @PathVariable String userId) {
        String decodedUserId = IdEncoder.decodeString(userId);
        List<ReadingListResponse> lists = readingListService.getReadingListsByUser(decodedUserId);
        return ResponseEntity.ok(WrapperApiResponse.success(lists));
    }

    @PutMapping("/{listId}/papers")
    @Operation(summary = "Add or remove papers from reading list", 
               description = "Update papers in a reading list. Action: 'add' or 'remove'. List ID and paper IDs should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Papers updated successfully"),
            @ApiResponse(responseCode = "404", description = "Reading list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid action or encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<ReadingListResponse>> updatePapers(
            @Parameter(description = "Encoded Reading List ID", required = true) @PathVariable String listId,
            @Valid @RequestBody ReadingListUpdatePapersRequest request) {
        String decodedListId = IdEncoder.decodeString(listId);
        // Decode paper IDs from request
        if (request.getPaperIds() != null) {
            request.setPaperIds(request.getPaperIds().stream()
                    .map(IdEncoder::decodeString)
                    .collect(java.util.stream.Collectors.toList()));
        }
        ReadingListResponse response = readingListService.updatePapers(decodedListId, request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @PutMapping("/{listId}/users")
    @Operation(summary = "Add or remove users from reading list", 
               description = "Update users in a reading list. Action: 'add' or 'remove'. List ID and user IDs should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users updated successfully"),
            @ApiResponse(responseCode = "404", description = "Reading list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid action or encoded IDs")
    })
    public ResponseEntity<WrapperApiResponse<ReadingListResponse>> updateUsers(
            @Parameter(description = "Encoded Reading List ID", required = true) @PathVariable String listId,
            @Valid @RequestBody ReadingListUpdateUsersRequest request) {
        String decodedListId = IdEncoder.decodeString(listId);
        // Decode user IDs from request
        if (request.getUserIds() != null) {
            request.setUserIds(request.getUserIds().stream()
                    .map(IdEncoder::decodeString)
                    .collect(java.util.stream.Collectors.toList()));
        }
        ReadingListResponse response = readingListService.updateUsers(decodedListId, request);
        return ResponseEntity.ok(WrapperApiResponse.success(response));
    }

    @DeleteMapping("/{listId}")
    @Operation(summary = "Delete a reading list", 
               description = "Delete a reading list by ID. List ID should be encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reading list deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Reading list not found"),
            @ApiResponse(responseCode = "400", description = "Invalid encoded list ID")
    })
    public ResponseEntity<WrapperApiResponse<String>> deleteReadingList(
            @Parameter(description = "Encoded Reading List ID", required = true) @PathVariable String listId) {
        String decodedListId = IdEncoder.decodeString(listId);
        readingListService.deleteReadingList(decodedListId);
        return ResponseEntity.ok(WrapperApiResponse.success("Reading list deleted successfully"));
    }
}

