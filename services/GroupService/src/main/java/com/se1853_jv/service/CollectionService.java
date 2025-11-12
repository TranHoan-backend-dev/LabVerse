package com.se1853_jv.service;

import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

import java.util.List;

public interface CollectionService {
    CollectionResponse createCollection(CollectionRequest request);

    CollectionResponse getCollectionById(String encodedId);

    Map<String, Object> getCollectionsManual(int page, int size);

    Map<String, Object> getMyCollections(String encodedUserId);

    Map<String, Object> getSharedCollections(String encodedUserId);

    CollectionPaperResponse addPaperToCollection(CollectionPaperRequest request);

    CollectionPaperResponse updatePaperStatus(CollectionPaperRequest request);

    void removePaperFromCollection(String encodedCollectionId, String encodedPaperId, String encodedUserId);

    List<CollectionPaperDetailResponse> getPapersInCollection(String encodedCollectionId);

    CollectionResponse updateCollection(String encodedCollectionId, UpdateCollectionRequest request);

    void deleteCollection(String encodedCollectionId, String encodedUserId);
    
    /**
     * Recalculate and update paper status in collection based on all members' reading progress
     * This is called after ReadingWorkflow is updated to keep collection status in sync
     */
    void recalculatePaperStatus(String encodedCollectionId, String encodedPaperId);
}

