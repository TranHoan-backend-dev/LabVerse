package com.se1853_jv.controller;

import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<WrapperApiResponse> create(@RequestBody CollectionRequest request) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.createCollection(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WrapperApiResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.getCollectionById(id)));
    }

    @GetMapping
    public ResponseEntity<WrapperApiResponse> getAll(Pageable pageable) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.getAllCollections(pageable)));
    }

    @PostMapping("/papers")
    public ResponseEntity<WrapperApiResponse> addPaper(@RequestBody CollectionPaperRequest request) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.addPaperToCollection(request)));
    }

    @PutMapping("/papers/status")
    public ResponseEntity<WrapperApiResponse> updateStatus(@RequestBody CollectionPaperRequest request) {
        return ResponseEntity.ok(WrapperApiResponse.success(collectionService.updatePaperStatus(request)));
    }
}
