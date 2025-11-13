package com.se1853_jv.labverse.data.api.readinglist;

import com.se1853_jv.labverse.data.dto.request.CreateReadingListRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateReadingListPapersRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateReadingListUsersRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.ReadingListResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReadingListApi {
    String READING_LISTS_PREFIX = "reading-lists";

    @POST(READING_LISTS_PREFIX)
    Call<BaseJsonResponse<ReadingListResponse>> createReadingList(@Body CreateReadingListRequest request);

    @GET(READING_LISTS_PREFIX + "/user/{userId}")
    Call<BaseJsonResponse<List<ReadingListResponse>>> getReadingListsByUser(@Path("userId") String userId);

    @GET(READING_LISTS_PREFIX + "/{listId}")
    Call<BaseJsonResponse<ReadingListResponse>> getReadingListById(@Path("listId") String listId);

    @PUT(READING_LISTS_PREFIX + "/{listId}/papers")
    Call<BaseJsonResponse<ReadingListResponse>> updatePapers(
            @Path("listId") String listId,
            @Body UpdateReadingListPapersRequest request
    );

    @PUT(READING_LISTS_PREFIX + "/{listId}/users")
    Call<BaseJsonResponse<ReadingListResponse>> updateUsers(
            @Path("listId") String listId,
            @Body UpdateReadingListUsersRequest request
    );

    @DELETE(READING_LISTS_PREFIX + "/{listId}")
    Call<BaseJsonResponse<Object>> deleteReadingList(@Path("listId") String listId);
}

