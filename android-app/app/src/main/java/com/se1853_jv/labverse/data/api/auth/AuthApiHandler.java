package com.se1853_jv.labverse.data.api.auth;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.ForgotPasswordRequest;
import com.se1853_jv.labverse.data.dto.request.GoogleLoginRequest;
import com.se1853_jv.labverse.data.dto.request.LoginRequest;
import com.se1853_jv.labverse.data.dto.request.RegisterRequest;
import com.se1853_jv.labverse.data.dto.response.AuthResponse;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthApiHandler {
    private static final String TAG = "AuthApiHandler";
    // 10.0.2.2 = localhost của máy host khi chạy trên Android Emulator
    // Port 8081 = AccountService port
    private static final String BASE_URL = "http://10.0.2.2:8081/api/auth/";
    
    private final AuthApi authApi;

    public AuthApiHandler() {
        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        var client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        
        authApi = retrofit.create(AuthApi.class);
    }

    /**
     * Đăng ký tài khoản mới
     */
    public void register(RegisterRequest request, ApiCallback<AuthResponse> callback) {
        Log.d(TAG, "register: " + request.getEmail());
        Call<BaseJsonResponse<AuthResponse>> call = authApi.register(request);
        call.enqueue(new Callback<BaseJsonResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<AuthResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Register successful: " + result.getUsername());
                } else {
                    String errorMessage = "Registration failed";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<AuthResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Đăng nhập
     */
    public void login(LoginRequest request, ApiCallback<AuthResponse> callback) {
        Log.d(TAG, "login: " + request.getEmail());
        Call<BaseJsonResponse<AuthResponse>> call = authApi.login(request);
        call.enqueue(new Callback<BaseJsonResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<AuthResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Login successful: " + result.getUsername());
                } else {
                    String errorMessage = "Login failed";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<AuthResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Đăng nhập bằng Google
     */
    public void googleLogin(GoogleLoginRequest request, ApiCallback<AuthResponse> callback) {
        Log.d(TAG, "googleLogin with token");
        Call<BaseJsonResponse<AuthResponse>> call = authApi.googleLogin(request);
        call.enqueue(new Callback<BaseJsonResponse<AuthResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<AuthResponse>> call, 
                                 @NonNull Response<BaseJsonResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Google login successful: " + result.getUsername());
                } else {
                    String errorMessage = "Google login failed";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<AuthResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Quên mật khẩu - Gửi email với mật khẩu mới
     */
    public void forgotPassword(ForgotPasswordRequest request, ApiCallback<String> callback) {
        Log.d(TAG, "forgotPassword: " + request.getEmail());
        Call<BaseJsonResponse<String>> call = authApi.forgotPassword(request);
        call.enqueue(new Callback<BaseJsonResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<String>> call, 
                                 @NonNull Response<BaseJsonResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    if (message == null || message.isEmpty()) {
                        message = response.body().getData();
                    }
                    callback.onSuccess(message != null ? message : "Password reset email sent successfully");
                    Log.d(TAG, "Forgot password successful: " + message);
                } else {
                    String errorMessage = "Failed to send password reset email";
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

    /**
     * Đăng xuất
     */
    public void logout(ApiCallback<String> callback) {
        Log.d(TAG, "logout");
        Call<BaseJsonResponse<String>> call = authApi.logout();
        call.enqueue(new Callback<BaseJsonResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<String>> call, 
                                 @NonNull Response<BaseJsonResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    callback.onSuccess(message != null ? message : "Logged out successfully");
                    Log.d(TAG, "Logout successful");
                } else {
                    // Even if server error, consider logout successful on client side
                    String errorMessage = "Logout completed";
                    Log.e(TAG, "Server Error but logout will proceed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<String>> call, @NonNull Throwable t) {
                // Even if network fails, consider logout successful on client side
                Log.e(TAG, "API Error but logout will proceed: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}

