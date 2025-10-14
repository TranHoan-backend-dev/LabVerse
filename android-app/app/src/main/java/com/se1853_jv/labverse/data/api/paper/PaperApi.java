package com.se1853_jv.labverse.data.api.paper;

import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PaperApi {
    @GET("/v1/api/papers/details")
    Call<PaperResearch> getPaperDetails(@Query("id") String id);
}
