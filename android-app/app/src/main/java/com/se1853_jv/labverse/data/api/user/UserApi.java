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
    
    @GET("users/me")
    Call<BaseJsonResponse<UserResponse>> getCurrentUser();
    
    @GET("users/email/{email}")
    Call<BaseJsonResponse<UserResponse>> getUserByEmail(@retrofit2.http.Path("email") String email);
    
    @GET("users/username/{username}")
    Call<BaseJsonResponse<UserResponse>> getUserByUsername(@retrofit2.http.Path("username") String username);
    
    @GET("users/{id}")
    Call<BaseJsonResponse<UserResponse>> getUserById(@retrofit2.http.Path("id") String id);
    
    @PATCH("users/me")
    Call<BaseJsonResponse<UserResponse>> updateProfile(@Body UpdateProfileRequest request);
    
    @PATCH("users/me/password")
    Call<BaseJsonResponse<String>> changePassword(@Body ChangePasswordRequest request);
}

