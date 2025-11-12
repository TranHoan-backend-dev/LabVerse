package com.se1853_jv.service.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.se1853_jv.dto.request.CollectionUserRequest;
import com.se1853_jv.dto.request.UpdateMemberAccessRequest;
import com.se1853_jv.dto.response.CollectionUserResponse;
import com.se1853_jv.exception.BadRequestException;
import com.se1853_jv.exception.DatabaseException;
import com.se1853_jv.exception.ResourceNotFoundException;
import com.se1853_jv.model.*;
import com.se1853_jv.model.Collection;
import com.se1853_jv.model.enumerate.AccessLevel;
import com.se1853_jv.repository.*;
import com.se1853_jv.dto.NotificationRequestEvent;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.service.CollectionUserService;
import com.se1853_jv.service.NotificationServiceClient;
import com.se1853_jv.service.UserServiceClient;
import com.se1853_jv.util.IdEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionUserServiceImpl implements CollectionUserService {

    private final CollectionRepository collectionRepository;
    private final CollectionUserRepository collectionUserRepository;
    private final Firestore firestore;
    private final NotificationServiceClient notificationServiceClient;
    private final UserServiceClient userServiceClient;

    private static final String FIRESTORE_COLLECTION = "collection_members";

    @Override
    public CollectionUserResponse addMember(CollectionUserRequest request) {
        String collectionId = IdEncoder.decode(request.getCollectionId());
        String memberId = IdEncoder.decode(request.getMemberId());

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + collectionId));

        CollectionUserId compositeId = new CollectionUserId();
        compositeId.setCollectionId(collectionId);
        compositeId.setMemberId(memberId);

        if (collectionUserRepository.existsById(compositeId)) {
            throw new DatabaseException("Member already in collection", null);
        }

        // Determine access level
        AccessLevel accessLevel;
        if (request.getAccessLevel() != null) {
            accessLevel = request.getAccessLevel();
        } else if (Boolean.TRUE.equals(request.getIsAuthor())) {
            // Backward compatibility: if isAuthor is true, set to AUTHOR
            accessLevel = AccessLevel.AUTHOR;
        } else {
            // Default to CONTRIBUTOR for new members
            accessLevel = AccessLevel.CONTRIBUTOR;
        }

        CollectionUser entity = CollectionUser.builder()
                .id(compositeId)
                .collection(collection)
                .isAuthor(request.getIsAuthor() != null ? request.getIsAuthor() : false)
                .accessLevel(accessLevel)
                .build();

        CollectionUser saved = collectionUserRepository.save(entity);
        CollectionUserResponse response = CollectionUserResponse.fromEntity(saved);
        
        // Populate user info (name, email, avatar, role)
        populateUserInfo(response, memberId);

        syncToFirestore(response);
        
        // Send notification to the new member
        sendAddMemberNotification(memberId, collection);
        
        return response;
    }

    @Override
    public void removeMember(String encodedCollectionId, String encodedMemberId) {
        String collectionId = IdEncoder.decode(encodedCollectionId);
        String memberId = IdEncoder.decode(encodedMemberId);

        CollectionUserId id = new CollectionUserId();
        id.setCollectionId(collectionId);
        id.setMemberId(memberId);

        if (!collectionUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member not found in this collection");
        }

        collectionUserRepository.deleteById(id);
        log.info("Removed member [{}] from collection [{}]", memberId, collectionId);
    }

    @Override
    public List<CollectionUserResponse> getMembers(String encodedCollectionId) {
        String collectionId = IdEncoder.decode(encodedCollectionId);

        List<CollectionUser> members = collectionUserRepository.findByIdCollectionId(collectionId);
        
        // Return empty list if no members found (frontend expects empty array, not exception)
        if (members.isEmpty()) {
            return new ArrayList<>();
        }

        return members.stream()
                .map(entity -> {
                    CollectionUserResponse response = CollectionUserResponse.fromEntity(entity);
                    String memberId = entity.getId().getMemberId();
                    populateUserInfo(response, memberId);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CollectionUserResponse updateMemberAccess(String encodedCollectionId, String encodedMemberId, UpdateMemberAccessRequest request) {
        String collectionId = IdEncoder.decode(encodedCollectionId);
        String memberId = IdEncoder.decode(encodedMemberId);
        String userId = IdEncoder.decode(request.getUserId());

        // Verify requester is AUTHOR
        CollectionUserId requesterId = new CollectionUserId();
        requesterId.setCollectionId(collectionId);
        requesterId.setMemberId(userId);
        CollectionUser requester = collectionUserRepository.findById(requesterId)
                .orElseThrow(() -> new BadRequestException("Requester is not a member of this collection"));
        
        if (requester.getAccessLevel() != AccessLevel.AUTHOR) {
            throw new BadRequestException("Only collection authors can update member access levels");
        }

        // Prevent author from changing their own access level
        if (memberId.equals(userId)) {
            throw new BadRequestException("Cannot change your own access level");
        }

        // Find the member to update
        CollectionUserId memberCompositeId = new CollectionUserId();
        memberCompositeId.setCollectionId(collectionId);
        memberCompositeId.setMemberId(memberId);
        CollectionUser member = collectionUserRepository.findById(memberCompositeId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this collection"));

        // Update access level
        member.setAccessLevel(request.getAccessLevel());
        // Also update isAuthor for backward compatibility
        member.setIsAuthor(request.getAccessLevel() == AccessLevel.AUTHOR);

        CollectionUser saved = collectionUserRepository.save(member);
        CollectionUserResponse response = CollectionUserResponse.fromEntity(saved);
        
        // Populate user info (name, email, avatar, role)
        populateUserInfo(response, memberId);
        
        syncToFirestore(response);
        
        return response;
    }

    private void syncToFirestore(CollectionUserResponse response) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("collectionId", response.getCollectionId());
            data.put("memberId", response.getMemberId());
            data.put("isAuthor", response.getIsAuthor());
            data.put("accessLevel", response.getAccessLevel() != null ? response.getAccessLevel().name() : null);
            data.put("timestamp", LocalDateTime.now().toString());

            ApiFuture<DocumentReference> future =
                    firestore.collection(FIRESTORE_COLLECTION).add(data);
            log.info("Synced member [{}] to Firestore: {}", response.getMemberId(), future.get().getId());
        } catch (InterruptedException | ExecutionException e) {
            throw new DatabaseException("Error syncing to Firestore", e);
        }
    }

    /**
     * Populate user information (name, email, avatar, role) from AccountService
     */
    private void populateUserInfo(CollectionUserResponse response, String memberId) {
        try {
            String encodedMemberId = IdEncoder.encode(memberId);
            WrapperApiResponse userResponse = userServiceClient.getUserById(encodedMemberId);
            
            if (userResponse != null && userResponse.getData() != null) {
                Object data = userResponse.getData();
                if (data instanceof Map) {
                    Map<String, Object> userMap = (Map<String, Object>) data;
                    response.setMemberName((String) userMap.getOrDefault("fullName", null));
                    response.setMemberEmail((String) userMap.getOrDefault("email", null));
                    response.setMemberAvatarUrl((String) userMap.getOrDefault("avatarUrl", null));
                    // Role is a String in UserResponse
                    response.setRole((String) userMap.getOrDefault("role", null));
                } else {
                    log.warn("Unexpected user data type for member ID {}: {}", memberId, data != null ? data.getClass().getName() : "null");
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching user info for member ID {}: {}", memberId, e.getMessage());
        }
    }

    /**
     * Send notification to user when they are added to a collection
     */
    private void sendAddMemberNotification(String memberId, Collection collection) {
        try {
            // Convert String ID to UUID
            UUID targetUserId = UUID.fromString(memberId);
            
            // Create notification event
            NotificationRequestEvent event = new NotificationRequestEvent();
            event.setTargetUserId(targetUserId);
            event.setTitle("You have been added to collection");
            event.setMessage(String.format("You have been added to collection \"%s\"", collection.getName()));
            event.setLinkTo("/collections/" + IdEncoder.encode(collection.getId()));
            
            // Send notification via Feign Client
            notificationServiceClient.createNotificationEvent(event);
            
            log.info("Sent notification to user [{}] for being added to collection [{}]", memberId, collection.getName());
        } catch (Exception e) {
            // Log error but don't fail the add member operation
            log.error("Failed to send notification to user [{}]: {}", memberId, e.getMessage(), e);
        }
    }
}
