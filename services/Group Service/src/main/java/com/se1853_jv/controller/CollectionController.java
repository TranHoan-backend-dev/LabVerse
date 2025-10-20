package com.se1853_jv.controller;

import com.se1853_jv.dto.CollectionDto;
import com.se1853_jv.dto.CollectionMemberDto;
import com.se1853_jv.dto.CollectionPaperDto;
import com.se1853_jv.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/groups/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<CollectionDto> create(@Valid @RequestBody CollectionDto dto) {
        return ResponseEntity.ok(collectionService.createCollection(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> get(@PathVariable String id) {
        return ResponseEntity.ok(collectionService.getCollectionById(id));
    }

    @GetMapping
    public ResponseEntity<List<CollectionDto>> getAll() {
        return ResponseEntity.ok(collectionService.getAllCollections());
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<CollectionMemberDto> addMember(@PathVariable String id, @Valid @RequestBody CollectionMemberDto dto) {
        return ResponseEntity.ok(collectionService.addMember(id, dto));
    }

    @DeleteMapping("/{id}/members/{memberRowId}")
    public ResponseEntity<Void> removeMember(@PathVariable String id, @PathVariable String memberRowId) {
        collectionService.removeMember(id, memberRowId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/papers")
    public ResponseEntity<CollectionPaperDto> addPaper(@PathVariable String id, @Valid @RequestBody CollectionPaperDto dto) {
        return ResponseEntity.ok(collectionService.addPaper(id, dto));
    }

    @DeleteMapping("/{id}/papers/{paperRowId}")
    public ResponseEntity<Void> removePaper(@PathVariable String id, @PathVariable String paperRowId) {
        collectionService.removePaper(id, paperRowId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }
}
