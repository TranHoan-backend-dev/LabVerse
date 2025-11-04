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
    CollectionPaperResponse addPaperToCollection(CollectionPaperRequest request);
    CollectionPaperResponse updatePaperStatus(CollectionPaperRequest request);
    List<CollectionPaperDetailResponse> getPapersInCollection(String encodedCollectionId);
}

