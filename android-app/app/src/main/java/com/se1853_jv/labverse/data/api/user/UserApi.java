package com.se1853_jv.labverse.data.api.user;

import com.se1853_jv.labverse.data.dto.request.ChangePasswordRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateProfileRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;

public interface UserApi {
    
    @GET("me")
    Call<BaseJsonResponse<UserResponse>> getCurrentUser();
    
    @PATCH("me")
    Call<BaseJsonResponse<UserResponse>> updateProfile(@Body UpdateProfileRequest request);
    
    @PATCH("me/password")
    Call<BaseJsonResponse<String>> changePassword(@Body ChangePasswordRequest request);
}

