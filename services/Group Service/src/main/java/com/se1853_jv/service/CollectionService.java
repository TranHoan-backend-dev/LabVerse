package com.se1853_jv.service;
import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CollectionService {
    CollectionResponse createCollection(CollectionRequest request);
    CollectionResponse getCollectionById(String encodedId);
    Page<CollectionResponse> getAllCollections(Pageable pageable);
    CollectionPaperResponse addPaperToCollection(CollectionPaperRequest request);
    CollectionPaperResponse updatePaperStatus(CollectionPaperRequest request);
}

