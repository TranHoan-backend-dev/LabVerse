package com.se1853_jv.labverse.data.api.paper;

import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
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
    @GET("papers")
    Call<BaseJsonResponse<List<PaperResearch>>> getAllPapers(
            @Query("search") String searchQuery,
            @Query("index") int currentPage,
            @Query("size") int pageSize
    );

    @HTTP(method = "DELETE", path = "{id}")
    Call<BaseJsonResponse<String>> deletePaper(@Path("id") String id);
}