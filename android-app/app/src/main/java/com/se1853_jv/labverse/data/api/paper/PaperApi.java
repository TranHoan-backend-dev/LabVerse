package com.se1853_jv.labverse.data.api.paper;

import com.se1853_jv.labverse.data.dto.request.UploadPdfRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface PaperApi {
    @GET("papers/details")
    Call<BaseJsonResponse<PaperResearch>> getPaperDetails(@Query("id") String id);
    @GET("papers/citation")
    Call<BaseJsonResponse<List<Citation>>> getCitationOfPaper(@Query("id") String id);
    @GET("papers/all")
    Call<BaseJsonResponse<List<PaperResearch>>> getAllPapers(
            @Query(value = "search", encoded = true) String searchQuery,
            @Query("index") int currentPage,
            @Query("size") int pageSize
    );

    @GET("papers/user/{userId}")
    Call<BaseJsonResponse<List<PaperResearch>>> getPapersByUserId(@Path("userId") String userId);

    @HTTP(method = "DELETE", path = "papers/{id}")
    Call<BaseJsonResponse<String>> deletePaper(@Path("id") String id);

    @POST("papers/pdf/upload")
    Call<BaseJsonResponse<Object>> uploadPdf(@Body UploadPdfRequest request, @Header("X-User-Id") String userId);
}