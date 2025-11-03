package com.se1853_jv.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.client.PaperServiceClient;
import com.se1853_jv.dto.response.PaperResponse;
import com.se1853_jv.exception.*;
import com.se1853_jv.model.*;
import com.se1853_jv.model.Collection;
import com.se1853_jv.repository.*;
import com.se1853_jv.service.CollectionService;
import com.se1853_jv.util.IdEncoder;
import feign.FeignException;
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
    private final PaperServiceClient paperServiceClient;
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

        // Validate paper exists in paper-service
        try {
            String encodedPaperId = IdEncoder.encode(paperId);
            var response = paperServiceClient.getPaperDetails(encodedPaperId);
            if (response == null || response.getData() == null) {
                log.warn("Paper not found in paper-service: {}", paperId);
                throw new ResourceNotFoundException("Paper not found: " + paperId);
            }
            log.info("Paper validated successfully: {}", paperId);
        } catch (FeignException.NotFound e) {
            log.error("Paper not found in paper-service: {}", paperId);
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        } catch (FeignException e) {
            log.error("Error calling paper-service for paper ID {}: {}", paperId, e.getMessage());
            throw new DatabaseException("Unable to validate paper with paper-service", e);
        } catch (Exception e) {
            log.error("Unexpected error validating paper ID {}: {}", paperId, e.getMessage());
            throw new ResourceNotFoundException("Paper not found: " + paperId);
        }

        CollectionPaperId compositeId = new CollectionPaperId(paperId, collectionId);

        // Kiểm tra tồn tại trong collection
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

    @Override
    public List<CollectionPaperDetailResponse> getPapersInCollection(String encodedCollectionId) {
        String collectionId = IdEncoder.decode(encodedCollectionId);
        
        // Verify collection exists
        collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + encodedCollectionId));
        
        List<CollectionPaper> collectionPapers = collectionPaperRepository.findByIdCollectionId(collectionId);
        
        return collectionPapers.stream().map(cp -> {
            String paperId = cp.getId().getPaperId();
            // Get paper details from paper-service
            try {
                String encodedPaperId = IdEncoder.encode(paperId);
                var paperResponse = paperServiceClient.getPaperDetails(encodedPaperId);
                if (paperResponse != null && paperResponse.getData() != null) {
                    PaperResponse paper = (PaperResponse) paperResponse.getData();
                    return CollectionPaperDetailResponse.builder()
                            .paperId(IdEncoder.encode(paperId))
                            .title(paper.getTitle())
                            .authors(paper.getAuthors())
                            .journal(paper.getJournal())
                            .publicationYear(paper.getPublicationYear())
                            .priority(cp.getPriority())
                            .status(cp.getStatus())
                            .addingDate(cp.getAddingDate())
                            .build();
                }
            } catch (Exception e) {
                log.error("Error fetching paper details for ID {}: {}", paperId, e.getMessage());
            }
            // Fallback if paper not found
            return CollectionPaperDetailResponse.builder()
                    .paperId(IdEncoder.encode(paperId))
                    .title("Unknown Paper")
                    .authors("")
                    .journal("")
                    .publicationYear(0)
                    .priority(cp.getPriority())
                    .status(cp.getStatus())
                    .addingDate(cp.getAddingDate())
                    .build();
        }).toList();
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
