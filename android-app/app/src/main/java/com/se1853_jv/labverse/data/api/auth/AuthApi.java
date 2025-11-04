package com.se1853_jv.labverse.data.api.auth;

import com.se1853_jv.labverse.data.dto.request.ForgotPasswordRequest;
import com.se1853_jv.labverse.data.dto.request.GoogleLoginRequest;
import com.se1853_jv.labverse.data.dto.request.LoginRequest;
import com.se1853_jv.labverse.data.dto.request.RegisterRequest;
import com.se1853_jv.labverse.data.dto.response.AuthResponse;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    
    @POST("register")
    Call<BaseJsonResponse<AuthResponse>> register(@Body RegisterRequest request);
    
    @POST("login")
    Call<BaseJsonResponse<AuthResponse>> login(@Body LoginRequest request);
    
    @POST("google")
    Call<BaseJsonResponse<AuthResponse>> googleLogin(@Body GoogleLoginRequest request);
    
    @POST("forgot-password")
    Call<BaseJsonResponse<String>> forgotPassword(@Body ForgotPasswordRequest request);
    
    @POST("logout")
    Call<BaseJsonResponse<String>> logout();
}

