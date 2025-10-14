package com.se1853_jv.labverse.data.service.paper;

import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiPaperService {
    @GET("/v1/api/papers/details")
    Call<PaperResearch> getPaperDetails(@Query("id") String id);
}
