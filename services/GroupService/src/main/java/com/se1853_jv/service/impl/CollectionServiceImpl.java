package com.se1853_jv.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.se1853_jv.dto.request.*;
import com.se1853_jv.dto.response.*;
import com.se1853_jv.dto.response.PaperProgressResponse;
import com.se1853_jv.dto.response.PaperResponse;
import com.se1853_jv.dto.response.UserPaperProgressResponse;
import com.se1853_jv.exception.*;
import com.se1853_jv.model.*;
import com.se1853_jv.model.Collection;
import com.se1853_jv.model.enumerate.AccessLevel;
import com.se1853_jv.repository.*;
import com.se1853_jv.service.CollectionService;
import com.se1853_jv.service.PaperService;
import com.se1853_jv.service.ReadingServiceClient;
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
    private final ReadingServiceClient readingServiceClient;
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

            // Verify user has PI role to create collection
            String userId = IdEncoder.decode(request.getUserId());
            try {
                String encodedUserId = IdEncoder.encode(userId);
                var userResponse = userServiceClient.getUserById(encodedUserId);
                
                if (userResponse == null || userResponse.getData() == null) {
                    throw new BadRequestException("User not found");
                }
                
                Object data = userResponse.getData();
                String userRole = null;
                
                // Handle different response types
                if (data instanceof Map) {
                    Map<String, Object> userMap = (Map<String, Object>) data;
                    userRole = (String) userMap.get("role");
                } else {
                    // Try to use reflection or handle as UserResponse object
                    try {
                        java.lang.reflect.Method getRoleMethod = data.getClass().getMethod("getRole");
                        Object roleObj = getRoleMethod.invoke(data);
                        userRole = roleObj != null ? roleObj.toString() : null;
                    } catch (Exception e) {
                        log.warn("Cannot extract role from user response: {}", e.getMessage());
                    }
                }
                
                if (userRole == null || !"PI".equalsIgnoreCase(userRole.trim())) {
                    throw new BadRequestException("Only users with PI role can create collections");
                }
            } catch (BadRequestException e) {
                throw e; // Re-throw BadRequestException as is
            } catch (FeignException.NotFound e) {
                log.error("User not found when creating collection: {}", userId);
                throw new BadRequestException("User not found");
            } catch (Exception e) {
                log.error("Error verifying user role when creating collection: {}", e.getMessage(), e);
                throw new BadRequestException("Unable to verify user permissions");
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
            // Note: userId was already decoded above for role verification
            CollectionUserId compositeId = new CollectionUserId();
            compositeId.setCollectionId(saved.getId());
            compositeId.setMemberId(userId);

            CollectionUser collectionUser = CollectionUser.builder()
                    .id(compositeId)
                    .collection(saved)
                    .isAuthor(true) // Creator is always author
                    .accessLevel(com.se1853_jv.model.enumerate.AccessLevel.AUTHOR) // Creator has AUTHOR access
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
        
        // Get counts
        long paperCount = collectionPaperRepository.findByIdCollectionId(entity.getId()).size();
        long memberCount = collectionUserRepository.findByIdCollectionId(entity.getId()).size();
        
        CollectionResponse response = CollectionResponse.fromEntity(entity, paperCount, memberCount);
        setCreatorInfo(response, entity.getId());
        return response;
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
                    // Set current user's access level
                    response.setCurrentUserAccessLevel(cu.getAccessLevel());
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
                    // Set current user's access level
                    response.setCurrentUserAccessLevel(cu.getAccessLevel());
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

        // Check authorization if userId is provided
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            String userId = IdEncoder.decode(request.getUserId());
            verifyUserCanAddPaper(collectionId, userId);
        }

        // Validate paper exists in paper-service
        try {
            String encodedPaperId = IdEncoder.encode(paperId);
            var response = paperServiceClient.getPaperDetails(encodedPaperId);
            if (response == null || response.getData() == null) {
                log.warn("Paper not found in paper-service: {}", paperId);
                throw new ResourceNotFoundException("Paper not found: " + paperId);
            }
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

        // Check authorization if userId is provided
        String currentPriority = entity.getPriority(); // Get current priority before update
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            String userId = IdEncoder.decode(request.getUserId());
            verifyUserCanUpdatePaperStatus(collectionId, userId, request.getPriority(), currentPriority);
        }

        // Status is now calculated automatically based on ReadingWorkflow of all members
        // Users cannot manually update status - it's read-only
        // Only update priority if it's provided and user has permission
        // If priority is null or empty, keep the current priority (don't update)
        if (request.getPriority() != null && !request.getPriority().isBlank()) {
            entity.setPriority(request.getPriority());
        }
        // If priority is null/empty, entity.getPriority() remains unchanged
        
        // Auto-calculate and update status based on all members' reading progress
        String calculatedStatus = calculatePaperStatus(collectionId, paperId);
        if (calculatedStatus != null) {
            entity.setStatus(calculatedStatus);
        }

        return CollectionPaperResponse.fromEntity(collectionPaperRepository.save(entity));
    }

    @Override
    public void removePaperFromCollection(String encodedCollectionId, String encodedPaperId, String encodedUserId) {
        String collectionId = IdEncoder.decode(encodedCollectionId);
        String paperId = IdEncoder.decode(encodedPaperId);
        String userId = IdEncoder.decode(encodedUserId);

        // Check authorization - only CONTRIBUTOR and AUTHOR can remove papers
        CollectionUserId compositeId = new CollectionUserId();
        compositeId.setCollectionId(collectionId);
        compositeId.setMemberId(userId);

        CollectionUser collectionUser = collectionUserRepository.findById(compositeId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this collection"));

        AccessLevel accessLevel = collectionUser.getAccessLevel();
        if (accessLevel == AccessLevel.READ_ONLY) {
            throw new BadRequestException("Read-only users cannot remove papers from the collection");
        }

        // Verify paper exists in collection
        CollectionPaperId paperCompositeId = new CollectionPaperId(paperId, collectionId);
        CollectionPaper collectionPaper = collectionPaperRepository.findById(paperCompositeId)
                .orElseThrow(() -> new ResourceNotFoundException("Paper not found in this collection"));

        // Remove paper from collection
        collectionPaperRepository.delete(collectionPaper);
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
                        // Calculate status automatically based on all members' reading progress
                        String calculatedStatus = calculatePaperStatus(collectionId, paperId);
                        String currentStatus = cp.getStatus();
                        
                        // Update status in database if it changed (case-insensitive comparison)
                        if (calculatedStatus != null && 
                            (currentStatus == null || !calculatedStatus.equalsIgnoreCase(currentStatus))) {
                            cp.setStatus(calculatedStatus);
                            cp = collectionPaperRepository.saveAndFlush(cp); // Save and flush immediately
                        }
                        
                        // Use status from entity (which may have been updated)
                        String statusToReturn = calculatedStatus != null ? calculatedStatus : cp.getStatus();
                        
                        return CollectionPaperDetailResponse.builder()
                                .paperId(IdEncoder.encode(paperId))
                                .title(paper.getTitle())
                                .authors(paper.getAuthors() != null ? paper.getAuthors() : "")
                                .journal(paper.getJournal() != null ? paper.getJournal() : "")
                                .publicationYear(paper.getPublicationYear() != null ? paper.getPublicationYear() : 0)
                                .priority(cp.getPriority())
                                .status(statusToReturn)
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
            // Calculate status automatically
            String calculatedStatus = calculatePaperStatus(collectionId, paperId);
            String currentStatus = cp.getStatus();
            
            // Update status in database if it changed (case-insensitive comparison)
            if (calculatedStatus != null && 
                (currentStatus == null || !calculatedStatus.equalsIgnoreCase(currentStatus))) {
                cp.setStatus(calculatedStatus);
                cp = collectionPaperRepository.saveAndFlush(cp); // Save and flush immediately
            }
            
            // Use status from entity (which may have been updated)
            String statusToReturn = calculatedStatus != null ? calculatedStatus : cp.getStatus();
            
            return CollectionPaperDetailResponse.builder()
                    .paperId(IdEncoder.encode(paperId))
                    .title("Unknown Paper")
                    .authors("Unknown Authors")
                    .journal("Unknown")
                    .publicationYear(0)
                    .priority(cp.getPriority())
                    .status(statusToReturn)
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
                            String email = (String) userMap.get("email");
                            String avatarUrl = (String) userMap.get("avatarUrl");
                            response.setCreatorName(fullName != null ? fullName : null);
                            response.setCreatorEmail(email != null ? email : null);
                            response.setCreatorAvatarUrl(avatarUrl != null ? avatarUrl : null);
                        } else {
                            // Try to use reflection or handle as UserResponse object
                            log.warn("Unexpected user data type for creator ID {}: {}", creatorId, data.getClass().getName());
                            response.setCreatorName(null);
                            response.setCreatorEmail(null);
                            response.setCreatorAvatarUrl(null);
                        }
                    } else {
                        response.setCreatorName(null);
                        response.setCreatorEmail(null);
                        response.setCreatorAvatarUrl(null);
                    }
                } catch (FeignException.NotFound e) {
                    log.warn("User not found for creator ID {}: {}", creatorId, e.getMessage());
                    response.setCreatorName(null);
                    response.setCreatorEmail(null);
                    response.setCreatorAvatarUrl(null);
                } catch (Exception e) {
                    log.warn("Error fetching user info for creator ID {}: {}", creatorId, e.getMessage());
                    response.setCreatorName(null);
                    response.setCreatorEmail(null);
                    response.setCreatorAvatarUrl(null);
                }
            } else {
                response.setCreatorName(null);
                response.setCreatorEmail(null);
                response.setCreatorAvatarUrl(null);
            }
        } catch (Exception e) {
            log.warn("Error setting creator info for collection {}: {}", collectionId, e.getMessage());
            response.setCreatorName(null);
            response.setCreatorEmail(null);
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

    /**
     * Verify that the user can add papers to the collection
     * READ_ONLY users cannot add papers
     * @param collectionId The collection ID
     * @param userId The user ID to verify
     * @throws BadRequestException if user does not have permission
     */
    private void verifyUserCanAddPaper(String collectionId, String userId) {
        CollectionUserId compositeId = new CollectionUserId();
        compositeId.setCollectionId(collectionId);
        compositeId.setMemberId(userId);

        CollectionUser collectionUser = collectionUserRepository.findById(compositeId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this collection"));

        AccessLevel accessLevel = collectionUser.getAccessLevel();
        if (accessLevel == AccessLevel.READ_ONLY) {
            throw new BadRequestException("Read-only users cannot add papers to the collection");
        }
    }

    /**
     * Verify that the user can update paper status/priority
     * - AUTHOR: Can update both status and priority
     * - CONTRIBUTOR: Can update status, but priority can only be set by AUTHOR
     * - READ_ONLY: Can update status only (cannot set priority)
     * @param collectionId The collection ID
     * @param userId The user ID to verify
     * @param priority The priority being set (null if only status is being updated)
     * @param currentPriority The current priority of the paper (to check if priority is being changed)
     * @throws BadRequestException if user does not have permission
     */
    private void verifyUserCanUpdatePaperStatus(String collectionId, String userId, String priority, String currentPriority) {
        CollectionUserId compositeId = new CollectionUserId();
        compositeId.setCollectionId(collectionId);
        compositeId.setMemberId(userId);

        CollectionUser collectionUser = collectionUserRepository.findById(compositeId)
                .orElseThrow(() -> new BadRequestException("User is not a member of this collection"));

        AccessLevel accessLevel = collectionUser.getAccessLevel();
        
        // All users (AUTHOR, CONTRIBUTOR, READ_ONLY) can update status
        // But only AUTHOR can change priority
        if (priority != null && !priority.isBlank()) {
            // Check if priority is being changed
            boolean isPriorityChanged = currentPriority == null || !priority.equals(currentPriority);
            if (isPriorityChanged && accessLevel != AccessLevel.AUTHOR) {
                throw new BadRequestException("Only collection authors can set paper priority");
            }
        }
    }

    /**
     * Calculate paper status automatically based on all members' reading progress
     * Status logic:
     * - "ToRead": No members have started reading (all ReadingWorkflow are unread or don't exist)
     * - "Reading": Some members are reading but not all have finished (at least one has progress > 0 and < 100, or not all members have progress = 100)
     * - "Finished": All members have finished reading (all ReadingWorkflow have progress = 100)
     * 
     * @param collectionId The collection ID
     * @param paperId The paper ID
     * @return Calculated status: "ToRead", "Reading", or "Finished", or null if error
     */
    private String calculatePaperStatus(String collectionId, String paperId) {
        try {
            // Get all members of the collection
            List<CollectionUser> members = collectionUserRepository.findByIdCollectionId(collectionId);
            if (members.isEmpty()) {
                // No members, default to "ToRead"
                return "ToRead";
            }
            
            int totalMembers = members.size();
            
            // Get paper progress from ReadingService
            String encodedCollectionId = IdEncoder.encode(collectionId);
            String encodedPaperId = IdEncoder.encode(paperId);
            
            PaperProgressResponse progressResponse;
            try {
                var wrapperResponse = readingServiceClient.getPaperProgress(encodedCollectionId, encodedPaperId);
                
                if (wrapperResponse == null || wrapperResponse.getData() == null) {
                    log.warn(">>> Could not get paper progress for collection {} paper {}, defaulting to ToRead", 
                            collectionId, paperId);
                    return "ToRead";
                }
                
                Object data = wrapperResponse.getData();
                
                // Handle different response types
                if (data instanceof PaperProgressResponse) {
                    progressResponse = (PaperProgressResponse) data;
                } else if (data instanceof Map) {
                    // Convert Map to PaperProgressResponse
                    Map<String, Object> progressMap = (Map<String, Object>) data;
                    
                    progressResponse = new PaperProgressResponse();
                    progressResponse.setPaperId((String) progressMap.get("paperId"));
                    if (progressMap.get("totalReaders") != null) {
                        progressResponse.setTotalReaders(((Number) progressMap.get("totalReaders")).longValue());
                    }
                    if (progressMap.get("unreadCount") != null) {
                        progressResponse.setUnreadCount(((Number) progressMap.get("unreadCount")).longValue());
                    }
                    if (progressMap.get("readingCount") != null) {
                        progressResponse.setReadingCount(((Number) progressMap.get("readingCount")).longValue());
                    }
                    if (progressMap.get("finishedCount") != null) {
                        progressResponse.setFinishedCount(((Number) progressMap.get("finishedCount")).longValue());
                    }
                    if (progressMap.get("averageProgress") != null) {
                        progressResponse.setAverageProgress(((Number) progressMap.get("averageProgress")).doubleValue());
                    }
                    // Handle userProgressList
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> userProgressMapList = (List<Map<String, Object>>) progressMap.get("userProgressList");
                    
                    if (userProgressMapList != null && !userProgressMapList.isEmpty()) {
                        List<UserPaperProgressResponse> userProgressList = userProgressMapList.stream()
                                .map(userMap -> {
                                    UserPaperProgressResponse userProgress = new UserPaperProgressResponse();
                                    userProgress.setUserId((String) userMap.get("userId"));
                                    userProgress.setStatus((String) userMap.get("status"));
                                    if (userMap.get("lastPage") != null) {
                                        userProgress.setLastPage(((Number) userMap.get("lastPage")).intValue());
                                    }
                                    if (userMap.get("progress") != null) {
                                        userProgress.setProgress(((Number) userMap.get("progress")).intValue());
                                    }
                                    return userProgress;
                                })
                                .collect(java.util.stream.Collectors.toList());
                        progressResponse.setUserProgressList(userProgressList);
                    }
                } else {
                    return "ToRead";
                }
            } catch (feign.FeignException.ServiceUnavailable e) {
                log.warn("ReadingService is not available (503), cannot calculate status for collection {} paper {}, defaulting to ToRead. Error: {}", 
                        collectionId, paperId, e.getMessage());
                return "ToRead";
            } catch (feign.FeignException e) {
                log.warn("Error calling ReadingService for collection {} paper {}: {}, defaulting to ToRead", 
                        collectionId, paperId, e.getMessage());
                return "ToRead";
            }
            
            
            // Use userProgressList to calculate status more accurately
            // This ensures we check actual progress for each member
            List<UserPaperProgressResponse> userProgressList = progressResponse.getUserProgressList();
            
            // Create a map of member IDs for quick lookup
            java.util.Set<String> memberIds = members.stream()
                    .map(m -> m.getId().getMemberId())
                    .collect(java.util.stream.Collectors.toSet());
            
            // Count members who have started reading (have workflow with progress > 0)
            long membersStarted = 0;
            long membersFinished = 0;
            
            if (userProgressList != null && !userProgressList.isEmpty()) {
                for (UserPaperProgressResponse userProgress : userProgressList) {
                    String userId = IdEncoder.decode(userProgress.getUserId());
                    Integer progress = userProgress.getProgress();
                    String status = userProgress.getStatus();
                    
                    // Check if this user is a member of the collection
                    if (memberIds.contains(userId)) {
                        // Count members who have started (progress > 0 or status is not unread)
                        if (progress != null && progress > 0) {
                            membersStarted++;
                            // Count members who have finished (progress = 100 or status = finished)
                            if (progress >= 100 || "finished".equalsIgnoreCase(status)) {
                                membersFinished++;
                            }
                        }
                    }
                }
            }
            
            // If no member has started reading
            if (membersStarted == 0) {
                return "ToRead";
            }
            
            // If all members have finished reading (progress = 100%)
            if (membersFinished == totalMembers) {
                return "Finished";
            }
            
            // If at least one member has started reading but not all have finished
            // This includes cases where some members haven't started yet
            if (membersStarted > 0) {
                return "Reading";
            }
            
            // Default: no one has started reading
            return "ToRead";
            
        } catch (Exception e) {
            log.error("Error calculating paper status for collection {} paper {}: {}", 
                    collectionId, paperId, e.getMessage(), e);
            // Return null to indicate error, caller will use existing status
            return null;
        }
    }

    @Override
    public void recalculatePaperStatus(String encodedCollectionId, String encodedPaperId) {
        try {
            String collectionId = IdEncoder.decode(encodedCollectionId);
            String paperId = IdEncoder.decode(encodedPaperId);
            
            CollectionPaperId compositeId = new CollectionPaperId(paperId, collectionId);
            CollectionPaper collectionPaper = collectionPaperRepository.findById(compositeId).orElse(null);
            
            if (collectionPaper == null) {
                log.warn("Paper not found in collection for status recalculation: collectionId={}, paperId={}", 
                        collectionId, paperId);
                return;
            }
            
            // Calculate new status
            String calculatedStatus = calculatePaperStatus(collectionId, paperId);
            String currentStatus = collectionPaper.getStatus();
            
            // Update if status changed (case-insensitive comparison)
            if (calculatedStatus != null && 
                (currentStatus == null || !calculatedStatus.equalsIgnoreCase(currentStatus))) {
                collectionPaper.setStatus(calculatedStatus);
                collectionPaperRepository.saveAndFlush(collectionPaper); // Save and flush immediately
            }
        } catch (Exception e) {
            log.error("Error recalculating paper status: collectionId={}, paperId={}, error={}", 
                    encodedCollectionId, encodedPaperId, e.getMessage(), e);
            // Don't throw exception - this is a background operation
        }
    }

    @Override
    public long getTotalCollectionsCount() {
        return collectionRepository.count();
    }

    private void storeToFirestore(CollectionResponse response) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("id", response.getId());
            data.put("name", response.getName());
            data.put("timestamp", LocalDateTime.now().toString());
            ApiFuture<DocumentReference> future = firestore.collection(COLLECTION_NAME).add(data);
            future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException("Error storing to Firestore", e);
        }
    }
}
