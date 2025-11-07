package com.se1853_jv.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.dto.response.PaperResponse;
import com.se1853_jv.exception.*;
import com.se1853_jv.model.*;
import com.se1853_jv.model.Collection;
import com.se1853_jv.repository.*;
import com.se1853_jv.service.CollectionService;
import com.se1853_jv.service.PaperService;
import com.se1853_jv.service.UserServiceClient;
import com.se1853_jv.util.IdEncoder;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionPaperRepository collectionPaperRepository;
    private final CollectionUserRepository collectionUserRepository;
    private final PaperService paperServiceClient;
    private final UserServiceClient userServiceClient;
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "collections";

    @Override
    public CollectionResponse createCollection(CollectionRequest request) {
        try {
            if (request.getName() == null || request.getName().isBlank()) {
                throw new IllegalArgumentException("Collection name must not be blank");
            }

            if (request.getUserId() == null || request.getUserId().isBlank()) {
                throw new IllegalArgumentException("User ID is required to create collection");
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

            // Automatically add creator as author (isAuthor = true)
            String userId = IdEncoder.decode(request.getUserId());
            CollectionUserId compositeId = new CollectionUserId();
            compositeId.setCollectionId(saved.getId());
            compositeId.setMemberId(userId);

            CollectionUser collectionUser = CollectionUser.builder()
                    .id(compositeId)
                    .collection(saved)
                    .isAuthor(true) // Creator is always author
                    .build();
            collectionUserRepository.save(collectionUser);

            // Get counts
            long paperCount = collectionPaperRepository.findByIdCollectionId(saved.getId()).size();
            long memberCount = collectionUserRepository.findByIdCollectionId(saved.getId()).size();

            CollectionResponse response = CollectionResponse.fromEntity(saved, paperCount, memberCount);
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
                .map(entity -> {
                    String collectionId = entity.getId();
                    // Count papers
                    long paperCount = collectionPaperRepository.findByIdCollectionId(collectionId).size();
                    // Count members
                    long memberCount = collectionUserRepository.findByIdCollectionId(collectionId).size();
                    CollectionResponse response = CollectionResponse.fromEntity(entity, paperCount, memberCount);
                    setCreatorInfo(response, collectionId);
                    return response;
                })
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
    public Map<String, Object> getMyCollections(String encodedUserId) {
        String userId = IdEncoder.decode(encodedUserId);

        // Get collections where user is author (isAuthor = true)
        List<CollectionUser> myCollectionUsers = collectionUserRepository.findByIdMemberIdAndIsAuthor(userId, true);

        List<CollectionResponse> myCollections = myCollectionUsers.stream()
                .map(cu -> {
                    Collection entity = cu.getCollection();
                    String collectionId = entity.getId();
                    long paperCount = collectionPaperRepository.findByIdCollectionId(collectionId).size();
                    long memberCount = collectionUserRepository.findByIdCollectionId(collectionId).size();
                    
                    // Get creator info (user with isAuthor = true)
                    CollectionResponse response = CollectionResponse.fromEntity(entity, paperCount, memberCount);
                    setCreatorInfo(response, collectionId);
                    return response;
                })
                .toList();

        return Map.of(
                "content", myCollections,
                "totalElements", myCollections.size()
        );
    }

    @Override
    public Map<String, Object> getSharedCollections(String encodedUserId) {
        String userId = IdEncoder.decode(encodedUserId);

        // Get collections where user is not author (isAuthor = false)
        List<CollectionUser> sharedCollectionUsers = collectionUserRepository.findByIdMemberIdAndIsAuthor(userId, false);

        List<CollectionResponse> sharedCollections = sharedCollectionUsers.stream()
                .map(cu -> {
                    Collection entity = cu.getCollection();
                    String collectionId = entity.getId();
                    long paperCount = collectionPaperRepository.findByIdCollectionId(collectionId).size();
                    long memberCount = collectionUserRepository.findByIdCollectionId(collectionId).size();
                    
                    // Get creator info (user with isAuthor = true)
                    CollectionResponse response = CollectionResponse.fromEntity(entity, paperCount, memberCount);
                    setCreatorInfo(response, collectionId);
                    return response;
                })
                .toList();

        return Map.of(
                "content", sharedCollections,
                "totalElements", sharedCollections.size()
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
            throw new DatabaseException("Paper already exists in this collection", null);
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
                var wrapperResponse = paperServiceClient.getPaperDetails(encodedPaperId);
                if (wrapperResponse != null && wrapperResponse.getData() != null) {
                    Object data = wrapperResponse.getData();
                    PaperResponse paper;

                    // Handle different response types
                    if (data instanceof PaperResponse) {
                        paper = (PaperResponse) data;
                    } else if (data instanceof Map) {
                        // Convert Map to PaperResponse
                        Map<String, Object> paperMap = (Map<String, Object>) data;
                        paper = new PaperResponse();
                        paper.setId((String) paperMap.get("id"));
                        paper.setTitle((String) paperMap.get("title"));
                        paper.setAuthors((String) paperMap.get("authors"));
                        paper.setJournal((String) paperMap.get("journal"));
                        if (paperMap.get("publicationYear") != null) {
                            if (paperMap.get("publicationYear") instanceof Integer) {
                                paper.setPublicationYear((Integer) paperMap.get("publicationYear"));
                            } else if (paperMap.get("publicationYear") instanceof Number) {
                                paper.setPublicationYear(((Number) paperMap.get("publicationYear")).intValue());
                            }
                        }
                        paper.setDataUrl((String) paperMap.get("dataUrl"));
                        paper.setDoi((String) paperMap.get("doi"));
                        paper.setDescription((String) paperMap.get("description"));
                        @SuppressWarnings("unchecked")
                        List<String> keywords = (List<String>) paperMap.get("keywords");
                        paper.setKeywords(keywords);
                    } else {
                        log.warn("Unexpected data type for paper ID {}: {}", paperId, data.getClass().getName());
                        paper = null;
                    }

                    if (paper != null && paper.getTitle() != null && !paper.getTitle().isEmpty()) {
                        return CollectionPaperDetailResponse.builder()
                                .paperId(IdEncoder.encode(paperId))
                                .title(paper.getTitle())
                                .authors(paper.getAuthors() != null ? paper.getAuthors() : "")
                                .journal(paper.getJournal() != null ? paper.getJournal() : "")
                                .publicationYear(paper.getPublicationYear() != null ? paper.getPublicationYear() : 0)
                                .priority(cp.getPriority())
                                .status(cp.getStatus())
                                .addingDate(cp.getAddingDate())
                                .build();
                    }
                }
            } catch (FeignException.NotFound e) {
                log.warn("Paper not found in paper-service for ID {}: {}", paperId, e.getMessage());
            } catch (Exception e) {
                log.error("Error fetching paper details for ID {}: {}", paperId, e.getMessage(), e);
            }

            // Fallback if paper not found or error
            return CollectionPaperDetailResponse.builder()
                    .paperId(IdEncoder.encode(paperId))
                    .title("Unknown Paper")
                    .authors("Unknown Authors")
                    .journal("Unknown")
                    .publicationYear(0)
                    .priority(cp.getPriority())
                    .status(cp.getStatus())
                    .addingDate(cp.getAddingDate())
                    .build();
        }).toList();
    }

    /**
     * Set creator information (name and avatar) for a collection response
     * Finds the user with isAuthor = true for the collection
     */
    private void setCreatorInfo(CollectionResponse response, String collectionId) {
        try {
            // Find creator (user with isAuthor = true)
            List<CollectionUser> collectionUsers = collectionUserRepository.findByIdCollectionId(collectionId);
            CollectionUser creator = collectionUsers.stream()
                    .filter(cu -> Boolean.TRUE.equals(cu.getIsAuthor()))
                    .findFirst()
                    .orElse(null);
            
            if (creator != null) {
                String creatorId = creator.getId().getMemberId();
                try {
                    // Call UserService to get creator name and avatarUrl
                    String encodedCreatorId = IdEncoder.encode(creatorId);
                    var userResponse = userServiceClient.getUserById(encodedCreatorId);
                    
                    if (userResponse != null && userResponse.getData() != null) {
                        Object data = userResponse.getData();
                        // Handle different response types
                        if (data instanceof Map) {
                            Map<String, Object> userMap = (Map<String, Object>) data;
                            String fullName = (String) userMap.get("fullName");
                            String avatarUrl = (String) userMap.get("avatarUrl");
                            response.setCreatorName(fullName != null ? fullName : null);
                            response.setCreatorAvatarUrl(avatarUrl != null ? avatarUrl : null);
                        } else {
                            // Try to use reflection or handle as UserResponse object
                            log.warn("Unexpected user data type for creator ID {}: {}", creatorId, data.getClass().getName());
                            response.setCreatorName(null);
                            response.setCreatorAvatarUrl(null);
                        }
                    } else {
                        response.setCreatorName(null);
                        response.setCreatorAvatarUrl(null);
                    }
                } catch (FeignException.NotFound e) {
                    log.warn("User not found for creator ID {}: {}", creatorId, e.getMessage());
                    response.setCreatorName(null);
                    response.setCreatorAvatarUrl(null);
                } catch (Exception e) {
                    log.warn("Error fetching user info for creator ID {}: {}", creatorId, e.getMessage());
                    response.setCreatorName(null);
                    response.setCreatorAvatarUrl(null);
                }
            } else {
                response.setCreatorName(null);
                response.setCreatorAvatarUrl(null);
            }
        } catch (Exception e) {
            log.warn("Error setting creator info for collection {}: {}", collectionId, e.getMessage());
            response.setCreatorName(null);
            response.setCreatorAvatarUrl(null);
        }
    }

    @Override
    public CollectionResponse updateCollection(String encodedCollectionId, UpdateCollectionRequest request) {
        try {
            String collectionId = IdEncoder.decode(encodedCollectionId);
            String userId = IdEncoder.decode(request.getUserId());

            // Verify collection exists
            Collection collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + encodedCollectionId));

            // Check authorization: user must be the author (isAuthor = true)
            verifyUserIsAuthor(collectionId, userId);

            // Validate name
            if (request.getName() == null || request.getName().isBlank()) {
                throw new IllegalArgumentException("Collection name must not be blank");
            }

            // Update collection name
            collection.setName(request.getName().trim());
            Collection saved = collectionRepository.save(collection);

            // Get counts
            long paperCount = collectionPaperRepository.findByIdCollectionId(saved.getId()).size();
            long memberCount = collectionUserRepository.findByIdCollectionId(saved.getId()).size();

            CollectionResponse response = CollectionResponse.fromEntity(saved, paperCount, memberCount);
            setCreatorInfo(response, saved.getId());
            storeToFirestore(response);
            return response;

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException || e instanceof BadRequestException) {
                throw e;
            }
            throw new DatabaseException("Error updating collection", e);
        }
    }

    @Override
    public void deleteCollection(String encodedCollectionId, String encodedUserId) {
        try {
            String collectionId = IdEncoder.decode(encodedCollectionId);
            String userId = IdEncoder.decode(encodedUserId);

            // Verify collection exists
            Collection collection = collectionRepository.findById(collectionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + encodedCollectionId));

            // Check authorization: user must be the author (isAuthor = true)
            verifyUserIsAuthor(collectionId, userId);

            // Delete all related collection papers
            List<CollectionPaper> papers = collectionPaperRepository.findByIdCollectionId(collectionId);
            collectionPaperRepository.deleteAll(papers);

            // Delete all related collection users
            List<CollectionUser> users = collectionUserRepository.findByIdCollectionId(collectionId);
            collectionUserRepository.deleteAll(users);

            // Delete the collection
            collectionRepository.delete(collection);

            log.info("Collection [{}] deleted successfully by user [{}]", collectionId, userId);

        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException || e instanceof BadRequestException) {
                throw e;
            }
            throw new DatabaseException("Error deleting collection", e);
        }
    }

    /**
     * Verify that the user is the author (creator) of the collection
     * @param collectionId The collection ID
     * @param userId The user ID to verify
     * @throws BadRequestException if user is not the author
     */
    private void verifyUserIsAuthor(String collectionId, String userId) {
        CollectionUserId compositeId = new CollectionUserId();
        compositeId.setCollectionId(collectionId);
        compositeId.setMemberId(userId);

        CollectionUser collectionUser = collectionUserRepository.findById(compositeId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this collection"));

        if (!Boolean.TRUE.equals(collectionUser.getIsAuthor())) {
            throw new BadRequestException("Only the collection author can perform this action");
        }
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
