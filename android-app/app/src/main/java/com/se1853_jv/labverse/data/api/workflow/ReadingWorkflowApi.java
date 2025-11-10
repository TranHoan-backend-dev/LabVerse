package com.se1853_jv.labverse.data.api.workflow;

import com.se1853_jv.labverse.data.dto.request.ReadingWorkflowProgressRequest;
import com.se1853_jv.labverse.data.dto.request.ReadingWorkflowStatusRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;

public interface ReadingWorkflowApi {

    @PUT("workflows/progress")
    Call<BaseJsonResponse<String>> updateProgress(@Body ReadingWorkflowProgressRequest request);

    @PATCH("workflows/status")
    Call<BaseJsonResponse<String>> updateStatus(@Body ReadingWorkflowStatusRequest request);
}

