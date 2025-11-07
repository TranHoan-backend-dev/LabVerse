package com.se1853_jv.controller;

import com.se1853_jv.dto.request.CollectionUserRequest;
import com.se1853_jv.dto.request.UpdateMemberAccessRequest;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.service.CollectionUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/collections/members")
@RequiredArgsConstructor
public class CollectionUserController {

    private final CollectionUserService collectionUserService;

    @PostMapping
    public ResponseEntity<WrapperApiResponse> addMember(@Valid @RequestBody CollectionUserRequest request) {
        return ResponseEntity.ok(
                WrapperApiResponse.success(collectionUserService.addMember(request))
        );
    }

    @DeleteMapping("/{collectionId}/{memberId}")
    public ResponseEntity<WrapperApiResponse> removeMember(
            @PathVariable String collectionId,
            @PathVariable String memberId) {
        collectionUserService.removeMember(collectionId, memberId);
        return ResponseEntity.ok(WrapperApiResponse.success("Member removed successfully"));
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<WrapperApiResponse> listMembers(@PathVariable String collectionId) {
        return ResponseEntity.ok(
                WrapperApiResponse.success(collectionUserService.getMembers(collectionId))
        );
    }

    @PutMapping("/{collectionId}/{memberId}/access")
    public ResponseEntity<WrapperApiResponse> updateMemberAccess(
            @PathVariable String collectionId,
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberAccessRequest request) {
        return ResponseEntity.ok(
                WrapperApiResponse.success(collectionUserService.updateMemberAccess(collectionId, memberId, request))
        );
    }
}
