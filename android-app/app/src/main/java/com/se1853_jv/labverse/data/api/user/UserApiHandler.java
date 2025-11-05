package com.se1853_jv.labverse.data.api.user;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.ChangePasswordRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateProfileRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.UserResponse;
import com.se1853_jv.labverse.data.utils.SessionManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserApiHandler {
    private static final String TAG = "UserApiHandler";
    private static final String BASE_URL = "http://10.0.2.2:8081/api/users/";
    
    private final UserApi userApi;
    private final SessionManager sessionManager;

    public UserApiHandler(Context context) {
        this.sessionManager = new SessionManager(context);
        
        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add token interceptor
        Interceptor tokenInterceptor = chain -> {
            Request original = chain.request();
            String token = sessionManager.getBearerToken();
            
            Request.Builder requestBuilder = original.newBuilder();
            if (token != null) {
                requestBuilder.header("Authorization", token);
            }
            
            return chain.proceed(requestBuilder.build());
        };

        var client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor)
                .addInterceptor(logging)
                .build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        
        userApi = retrofit.create(UserApi.class);
    }

    /**
     * Lấy thông tin user hiện tại
     */
    public void getCurrentUser(ApiCallback<UserResponse> callback) {
        Log.d(TAG, "getCurrentUser");
        Call<BaseJsonResponse<UserResponse>> call = userApi.getCurrentUser();
        call.enqueue(new Callback<BaseJsonResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<UserResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Get current user successful: " + result.getUsername());
                } else {
                    String errorMessage = "Failed to get user information";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<UserResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Cập nhật profile
     */
    public void updateProfile(UpdateProfileRequest request, ApiCallback<UserResponse> callback) {
        Log.d(TAG, "updateProfile: " + request.getUsername());
        Call<BaseJsonResponse<UserResponse>> call = userApi.updateProfile(request);
        call.enqueue(new Callback<BaseJsonResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<UserResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Update profile successful: " + result.getUsername());
                } else {
                    String errorMessage = "Failed to update profile";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<UserResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Tìm user bằng email (để invite vào collection)
     */
    public void getUserByEmail(String email, ApiCallback<UserResponse> callback) {
        Log.d(TAG, "getUserByEmail: " + email);
        Call<BaseJsonResponse<UserResponse>> call = userApi.getUserByEmail(email);
        call.enqueue(new Callback<BaseJsonResponse<UserResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<UserResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Get user by email successful: " + result.getEmail());
                } else {
                    String errorMessage = "User not found with email: " + email;
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<UserResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Đổi mật khẩu
     */
    public void changePassword(ChangePasswordRequest request, ApiCallback<String> callback) {
        Log.d(TAG, "changePassword");
        Call<BaseJsonResponse<String>> call = userApi.changePassword(request);
        call.enqueue(new Callback<BaseJsonResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<String>> call, 
                                 @NonNull Response<BaseJsonResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    callback.onSuccess(message != null ? message : "Password changed successfully");
                    Log.d(TAG, "Change password successful");
                } else {
                    String errorMessage = "Failed to change password";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    Log.e(TAG, "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<String>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}

