package com.se1853_jv.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.exception.*;
import com.se1853_jv.model.*;
import com.se1853_jv.model.Collection;
import com.se1853_jv.repository.*;
import com.se1853_jv.service.CollectionService;
import com.se1853_jv.util.IdEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionPaperRepository collectionPaperRepository;
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "collections";

    @Override
    public CollectionResponse createCollection(CollectionRequest request) {
        try {
            if (request.getName() == null || request.getName().isBlank()) {
                throw new IllegalArgumentException("Collection name must not be blank");
            }

            // Kiểm tra trùng tên, nếu trùng thì thêm (1), (2)
            String baseName = request.getName().trim();
            String newName = baseName;
            int counter = 1;

            while (collectionRepository.existsByName(newName)) {
                newName = baseName + " (" + counter + ")";
                counter++;
            }

            Collection entity = Collection.builder()
                    .id(UUID.randomUUID().toString())
                    .name(request.getName())
                    .build();

            Collection saved = collectionRepository.save(entity);
            CollectionResponse response = CollectionResponse.fromEntity(saved);
            storeToFirestore(response);
            return response;

        } catch (Exception e) {
            throw new DatabaseException("Error creating collection", e);
        }
    }

    @Override
    public CollectionResponse getCollectionById(String encodedId) {
        String decodedId = IdEncoder.decode(encodedId);
        Collection entity = collectionRepository.findById(decodedId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + encodedId));
        return CollectionResponse.fromEntity(entity);
    }

    @Override
    public Map<String, Object> getCollectionsManual(int page, int size) {
        List<Collection> all = collectionRepository.findAll();

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, all.size());

        if (fromIndex >= all.size()) {
            return Map.of(
                    "content", Collections.emptyList(),
                    "page", page,
                    "size", size,
                    "totalElements", all.size(),
                    "totalPages", (int) Math.ceil((double) all.size() / size)
            );
        }

        List<CollectionResponse> pageContent = all.subList(fromIndex, toIndex)
                .stream()
                .map(CollectionResponse::fromEntity)
                .toList();

        return Map.of(
                "content", pageContent,
                "page", page,
                "size", size,
                "totalElements", all.size(),
                "totalPages", (int) Math.ceil((double) all.size() / size)
        );
    }

    @Override
    public CollectionPaperResponse addPaperToCollection(CollectionPaperRequest request) {
        String collectionId = IdEncoder.decode(request.getCollectionId());
        String paperId = IdEncoder.decode(request.getPaperId());

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + collectionId));

        CollectionPaperId compositeId = new CollectionPaperId(paperId, collectionId);

        // Kiểm tra tồn tại
        if (collectionPaperRepository.existsById(compositeId)) {
            throw new DatabaseException("Paper already exists in this collection",null);
        }

        CollectionPaper entity = CollectionPaper.builder()
                .id(compositeId)
                .collection(collection)
                .priority(request.getPriority())
                .status(request.getStatus())
                .addingDate(LocalDate.now())
                .build();

        CollectionPaper saved = collectionPaperRepository.save(entity);
        return CollectionPaperResponse.fromEntity(saved);
    }

    @Override
    public CollectionPaperResponse updatePaperStatus(CollectionPaperRequest request) {
        String collectionId = IdEncoder.decode(request.getCollectionId());
        String paperId = IdEncoder.decode(request.getPaperId());

        CollectionPaperId id = new CollectionPaperId(paperId, collectionId);
        CollectionPaper entity = collectionPaperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paper not found in collection"));

        entity.setPriority(request.getPriority());
        entity.setStatus(request.getStatus());

        return CollectionPaperResponse.fromEntity(collectionPaperRepository.save(entity));
    }

    private void storeToFirestore(CollectionResponse response) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", response.getId());
            data.put("name", response.getName());
            data.put("timestamp", LocalDateTime.now().toString());
            ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME).add(data);
            log.info("Stored collection [{}] into Firestore: {}", response.getName(), future.get().getId());
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException("Error storing to Firestore", e);
        }
    }
}
