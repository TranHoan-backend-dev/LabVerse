package com.se1853_jv.service;

import com.se1853_jv.dto.CollectionDto;
import com.se1853_jv.dto.CollectionMemberDto;
import com.se1853_jv.dto.CollectionPaperDto;

import java.util.List;

public interface CollectionService {
    CollectionDto createCollection(CollectionDto dto);
    CollectionDto getCollectionById(String id);
    List<CollectionDto> getAllCollections();
    CollectionMemberDto addMember(String collectionId, CollectionMemberDto dto);
    void removeMember(String collectionId, String memberRowId);
    CollectionPaperDto addPaper(String collectionId, CollectionPaperDto dto);
    void removePaper(String collectionId, String paperRowId);
    void deleteCollection(String id);
}