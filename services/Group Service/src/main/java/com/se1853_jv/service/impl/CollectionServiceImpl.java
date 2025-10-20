package com.se1853_jv.service.impl;

import com.se1853_jv.dto.*;
import com.se1853_jv.exception.ResourceNotFoundException;
import com.se1853_jv.model.Collection;
import com.se1853_jv.model.CollectionPaper;
import com.se1853_jv.model.CollectionUser;
import com.se1853_jv.repository.CollectionPaperRepository;
import com.se1853_jv.repository.CollectionRepository;
import com.se1853_jv.repository.CollectionUserRepository;
import com.se1853_jv.service.CollectionService;
import com.se1853_jv.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionUserRepository collectionUserRepository;
    private final CollectionPaperRepository collectionPaperRepository;

    @Override
    public CollectionDto createCollection(CollectionDto dto) {
        Collection c = Collection.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .build();
        c = collectionRepository.save(c);
        return MapperUtil.toCollectionDto(c);
    }

    @Override
    public CollectionDto getCollectionById(String id) {
        Collection c = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + id));
        return MapperUtil.toCollectionDto(c);
    }

    @Override
    public List<CollectionDto> getAllCollections() {
        return collectionRepository.findAll().stream().map(MapperUtil::toCollectionDto).collect(Collectors.toList());
    }

    @Override
    public CollectionMemberDto addMember(String collectionId, CollectionMemberDto dto) {
        Collection c = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + collectionId));
        CollectionUser cu = CollectionUser.builder()
                .id(UUID.randomUUID().toString())
                .memberId(dto.getMemberId())
                .isAuthor(dto.isAuthor())
                .collection(c)
                .build();
        cu = collectionUserRepository.save(cu);
        c.getMembers().add(cu);
        collectionRepository.save(c);
        return MapperUtil.toCollectionMemberDto(cu);
    }

    @Override
    public void removeMember(String collectionId, String memberRowId) {
        CollectionUser cu = collectionUserRepository.findById(memberRowId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection member not found: " + memberRowId));
        collectionUserRepository.delete(cu);
    }

    @Override
    public CollectionPaperDto addPaper(String collectionId, CollectionPaperDto dto) {
        Collection c = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + collectionId));
        CollectionPaper cp = CollectionPaper.builder()
                .id(UUID.randomUUID().toString())
                .paperId(dto.getPaperId())
                .priority(dto.getPriority())
                .status(dto.getStatus())
                .addingDate(LocalDateTime.now())
                .collection(c)
                .build();
        cp = collectionPaperRepository.save(cp);
        c.getPapers().add(cp);
        collectionRepository.save(c);
        return MapperUtil.toCollectionPaperDto(cp);
    }

    @Override
    public void removePaper(String collectionId, String paperRowId) {
        CollectionPaper cp = collectionPaperRepository.findById(paperRowId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection paper not found: " + paperRowId));
        collectionPaperRepository.delete(cp);
    }

    @Override
    public void deleteCollection(String id) {
        Collection c = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + id));
        collectionRepository.delete(c);
    }
}