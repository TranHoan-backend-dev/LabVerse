package com.se1853_jv.labverse.data.api.paper;

import com.se1853_jv.labverse.data.dto.request.UploadPdfRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.PapersPageResponse;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface PaperApi {
    @GET("details")
    Call<BaseJsonResponse<PaperResearch>> getPaperDetails(@Query("id") String id);
    @GET("citation")
    Call<BaseJsonResponse<List<Citation>>> getCitationOfPaper(@Query("id") String id);
    @GET("all")
    Call<BaseJsonResponse<PapersPageResponse>> getAllPapers(
            @Query(value = "search", encoded = true) String searchQuery,
            @Query("index") int currentPage,
            @Query("size") int pageSize,
            @Query(value = "author", encoded = true) String author,
            @Query(value = "journal", encoded = true) String journal,
            @Query("from") Integer yearFrom,
            @Query("to") Integer yearTo
    );

    @GET("user/{userId}")
    Call<BaseJsonResponse<List<PaperResearch>>> getPapersByUserId(@Path("userId") String userId);

    @HTTP(method = "DELETE", path = "{id}")
    Call<BaseJsonResponse<String>> deletePaper(@Path("id") String id);

    @POST("pdf/upload")
    Call<BaseJsonResponse<Object>> uploadPdf(@Body UploadPdfRequest request, @Header("X-User-Id") String userId);
}