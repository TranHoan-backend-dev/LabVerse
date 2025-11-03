package com.se1853_jv.labverse.data.api.paper;

import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

import java.util.List;

public interface PaperApi {
    @GET("details")
    Call<BaseJsonResponse<PaperResearch>> getPaperDetails(@Query("id") String id);
    @GET("citation")
    Call<BaseJsonResponse<List<Citation>>> getCitationOfPaper(@Query("id") String id);
    @GET()
    Call<BaseJsonResponse<List<PaperResearch>>> getAllPapers(@Url String url, @Query("search") String searchQuery);
}
