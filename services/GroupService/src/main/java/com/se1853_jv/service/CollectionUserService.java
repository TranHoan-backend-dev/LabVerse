package com.se1853_jv.service;


import com.se1853_jv.dto.request.CollectionUserRequest;
import com.se1853_jv.dto.request.UpdateMemberAccessRequest;
import com.se1853_jv.dto.response.CollectionUserResponse;

import java.util.List;

public interface CollectionUserService {
    CollectionUserResponse addMember(CollectionUserRequest request);

    void removeMember(String encodedCollectionId, String encodedMemberId);

    List<CollectionUserResponse> getMembers(String encodedCollectionId);

    CollectionUserResponse updateMemberAccess(String encodedCollectionId, String encodedMemberId, UpdateMemberAccessRequest request);
}