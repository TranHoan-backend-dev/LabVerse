package com.se1853_jv.labverse.data.api.collection;

import com.se1853_jv.labverse.data.dto.request.CollectionRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionsPageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CollectionApi {
    @GET("collections")
    Call<BaseJsonResponse<CollectionsPageResponse>> getCollections(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("collections/{id}")
    Call<BaseJsonResponse<CollectionResponse>> getCollectionById(@Path("id") String id);

    @POST("collections")
    Call<BaseJsonResponse<CollectionResponse>> createCollection(@Body CollectionRequest request);
}


