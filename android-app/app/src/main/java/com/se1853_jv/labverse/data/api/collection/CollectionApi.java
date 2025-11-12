package com.se1853_jv.labverse.data.api.collection;

import com.se1853_jv.labverse.data.dto.request.CollectionRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionUserRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateCollectionRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateMemberAccessRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperDetailResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionsPageResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionUserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CollectionApi {
    @GET("collections/my")
    Call<BaseJsonResponse<CollectionsPageResponse>> getMyCollections(
            @Query("userId") String encodedUserId
    );

    @GET("collections/shared")
    Call<BaseJsonResponse<CollectionsPageResponse>> getSharedCollections(
            @Query("userId") String encodedUserId
    );

    @GET("collections/{id}")
    Call<BaseJsonResponse<CollectionResponse>> getCollectionById(@Path("id") String id);

    @POST("collections")
    Call<BaseJsonResponse<CollectionResponse>> createCollection(@Body CollectionRequest request);

    @POST("collections/papers")
    Call<BaseJsonResponse<CollectionPaperResponse>> addPaperToCollection(@Body CollectionPaperRequest request);

    @GET("collections/{id}/papers")
    Call<BaseJsonResponse<List<CollectionPaperDetailResponse>>> getPapersInCollection(@Path("id") String id);

    @retrofit2.http.PUT("collections/papers/status")
    Call<BaseJsonResponse<CollectionPaperResponse>> updatePaperStatus(@Body CollectionPaperRequest request);

    @POST("collections/members")
    Call<BaseJsonResponse<Object>> addMemberToCollection(@Body CollectionUserRequest request);

    @retrofit2.http.PUT("collections/{id}")
    Call<BaseJsonResponse<CollectionResponse>> updateCollection(
            @Path("id") String id,
            @Body UpdateCollectionRequest request);

    @retrofit2.http.DELETE("collections/{id}")
    Call<BaseJsonResponse<Object>> deleteCollection(
            @Path("id") String id,
            @Query("userId") String encodedUserId);

    @retrofit2.http.DELETE("collections/{collectionId}/papers/{paperId}")
    Call<BaseJsonResponse<Object>> removePaperFromCollection(
            @Path("collectionId") String collectionId,
            @Path("paperId") String paperId,
            @Query("userId") String encodedUserId);

    @GET("collections/members/{collectionId}")
    Call<BaseJsonResponse<List<CollectionUserResponse>>> getMembers(@Path("collectionId") String collectionId);

    @retrofit2.http.DELETE("collections/members/{collectionId}/{memberId}")
    Call<BaseJsonResponse<Object>> removeMember(
            @Path("collectionId") String collectionId,
            @Path("memberId") String memberId);

    @retrofit2.http.PUT("collections/members/{collectionId}/{memberId}/access")
    Call<BaseJsonResponse<CollectionUserResponse>> updateMemberAccess(
            @Path("collectionId") String collectionId,
            @Path("memberId") String memberId,
            @Body UpdateMemberAccessRequest request);
    
    @POST("collections/papers/recalculate-status")
    Call<BaseJsonResponse<Object>> recalculatePaperStatus(
            @Query("collectionId") String encodedCollectionId,
            @Query("paperId") String encodedPaperId);
}


