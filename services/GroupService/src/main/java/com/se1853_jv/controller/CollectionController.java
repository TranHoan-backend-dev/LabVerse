package com.se1853_jv.controller;

import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<WrapperApiResponse> create(@Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.createCollection(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WrapperApiResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.getCollectionById(id)));
    }

    @GetMapping
    public ResponseEntity<WrapperApiResponse> getAll (
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                WrapperApiResponse.success(collectionService.getCollectionsManual(page, size))
        );
    }

    @GetMapping("/my")
    public ResponseEntity<WrapperApiResponse> getMyCollections(
            @RequestParam("userId") String encodedUserId) {
        return ResponseEntity.ok(
                WrapperApiResponse.success(collectionService.getMyCollections(encodedUserId))
        );
    }

    @GetMapping("/shared")
    public ResponseEntity<WrapperApiResponse> getSharedCollections(
            @RequestParam("userId") String encodedUserId) {
        return ResponseEntity.ok(
                WrapperApiResponse.success(collectionService.getSharedCollections(encodedUserId))
        );
    }

    @PostMapping("/papers")
    public ResponseEntity<WrapperApiResponse> addPaper(@Valid @RequestBody CollectionPaperRequest request) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.addPaperToCollection(request)));
    }

    @PutMapping("/papers/status")
    public ResponseEntity<WrapperApiResponse> updateStatus(@Valid @RequestBody CollectionPaperRequest request) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.updatePaperStatus(request)));
    }

    @DeleteMapping("/{collectionId}/papers/{paperId}")
    public ResponseEntity<WrapperApiResponse> removePaper(
            @PathVariable String collectionId,
            @PathVariable String paperId,
            @RequestParam("userId") String encodedUserId) {
        collectionService.removePaperFromCollection(collectionId, paperId, encodedUserId);
        return ResponseEntity.ok(WrapperApiResponse.success("Paper removed from collection successfully"));
    }

    @GetMapping("/{id}/papers")
    public ResponseEntity<WrapperApiResponse> getPapersInCollection(@PathVariable String id) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.getPapersInCollection(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WrapperApiResponse> updateCollection(
            @PathVariable String id,
            @Valid @RequestBody UpdateCollectionRequest request) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.updateCollection(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WrapperApiResponse> deleteCollection(
            @PathVariable String id,
            @RequestParam("userId") String encodedUserId) {
        collectionService.deleteCollection(id, encodedUserId);
        return ResponseEntity.ok(WrapperApiResponse.success("Collection deleted successfully"));
    }
    
    @PostMapping("/papers/recalculate-status")
    public ResponseEntity<WrapperApiResponse> recalculatePaperStatus(
            @RequestParam("collectionId") String encodedCollectionId,
            @RequestParam("paperId") String encodedPaperId) {
        collectionService.recalculatePaperStatus(encodedCollectionId, encodedPaperId);
        return ResponseEntity.ok(WrapperApiResponse.success("Paper status recalculated successfully"));
    }
    
    @GetMapping("/internal/statistics")
    public ResponseEntity<WrapperApiResponse> getStatistics() {
        long totalCollections = collectionService.getTotalCollectionsCount();
        return ResponseEntity.ok(WrapperApiResponse.success(Map.of("totalCollections", totalCollections)));
    }
}
