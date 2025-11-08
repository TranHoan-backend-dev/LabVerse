package com.se1853_jv.labverse.data.service.unpaywall;

import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.UnpaywallResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UnpaywallApi {
    @GET("{doi}")
    Call<BaseJsonResponse<UnpaywallResponse>> getUrlForPdf(
            @Path("doi") String doi,
            @Query("email") String email
    );
}
