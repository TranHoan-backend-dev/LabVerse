package com.se1853_jv.controller;

import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/group/collections")
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
    public ResponseEntity<WrapperApiResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                WrapperApiResponse.success(collectionService.getCollectionsManual(page, size))
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
}
