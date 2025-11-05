package com.se1853_jv.labverse.data.api.collection;

import com.se1853_jv.labverse.data.dto.request.CollectionRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionUserRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperDetailResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionsPageResponse;

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
}


