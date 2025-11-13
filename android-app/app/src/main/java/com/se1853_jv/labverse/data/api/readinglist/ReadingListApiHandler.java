package com.se1853_jv.labverse.data.api.readinglist;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.CreateReadingListRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateReadingListPapersRequest;
import com.se1853_jv.labverse.data.dto.request.UpdateReadingListUsersRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.ReadingListResponse;
import com.se1853_jv.labverse.data.utils.EncoderUtils;
import com.se1853_jv.labverse.data.utils.SessionManager;

import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReadingListApiHandler {
    private static final String TAG = "ReadingListApiHandler";
    private static final String BASE_URL = "http://10.0.2.2:8080/reading-service/";

    private final ReadingListApi readingListApi;
    private final SessionManager sessionManager;

    public ReadingListApiHandler(Context context) {
        this.sessionManager = new SessionManager(context);

        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

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
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        readingListApi = retrofit.create(ReadingListApi.class);
    }

    /**
     * Create a new reading list
     */
    public void createReadingList(CreateReadingListRequest request, ApiCallback<ReadingListResponse> callback) {
        Log.d(TAG, "Creating reading list: " + request.getName());
        Call<BaseJsonResponse<ReadingListResponse>> call = readingListApi.createReadingList(request);
        call.enqueue(new Callback<BaseJsonResponse<ReadingListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call,
                                 @NonNull Response<BaseJsonResponse<ReadingListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Reading list created: " + (result != null ? result.getId() : "null"));
                } else {
                    String errorMessage = "Failed to create reading list";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Get reading list by ID
     * Note: listId should be encoded before calling
     */
    public void getReadingListById(String listId, ApiCallback<ReadingListResponse> callback) {
        // listId from API response is already encoded, use directly
        Log.d(TAG, "Getting reading list by ID: " + listId);
        Call<BaseJsonResponse<ReadingListResponse>> call = readingListApi.getReadingListById(listId);
        call.enqueue(new Callback<BaseJsonResponse<ReadingListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call,
                                 @NonNull Response<BaseJsonResponse<ReadingListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Reading list retrieved: " + (result != null ? result.getId() : "null"));
                } else {
                    String errorMessage = "Failed to get reading list";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Get reading lists by user ID
     * Note: userId should be encoded before calling
     */
    public void getReadingListsByUser(String userId, ApiCallback<List<ReadingListResponse>> callback) {
        // Encode userId
        String encodedUserId = EncoderUtils.encode(userId);
        Log.d(TAG, "Getting reading lists for user: " + userId + " (encoded: " + encodedUserId + ")");
        
        Call<BaseJsonResponse<List<ReadingListResponse>>> call = readingListApi.getReadingListsByUser(encodedUserId);
        call.enqueue(new Callback<BaseJsonResponse<List<ReadingListResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<List<ReadingListResponse>>> call,
                                 @NonNull Response<BaseJsonResponse<List<ReadingListResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Reading lists retrieved: " + (result != null ? result.size() : 0) + " lists");
                } else {
                    String errorMessage = "Failed to get reading lists";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<List<ReadingListResponse>>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Update papers in reading list
     */
    public void updatePapers(String listId, UpdateReadingListPapersRequest request, ApiCallback<ReadingListResponse> callback) {
        // listId from API response is already encoded, use directly
        Log.d(TAG, "Updating papers in list: " + listId);
        Call<BaseJsonResponse<ReadingListResponse>> call = readingListApi.updatePapers(listId, request);
        call.enqueue(new Callback<BaseJsonResponse<ReadingListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call,
                                 @NonNull Response<BaseJsonResponse<ReadingListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Papers updated successfully");
                } else {
                    String errorMessage = "Failed to update papers";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Update users in reading list
     */
    public void updateUsers(String listId, UpdateReadingListUsersRequest request, ApiCallback<ReadingListResponse> callback) {
        // listId from API response is already encoded, use directly
        Log.d(TAG, "Updating users in list: " + listId);
        Call<BaseJsonResponse<ReadingListResponse>> call = readingListApi.updateUsers(listId, request);
        call.enqueue(new Callback<BaseJsonResponse<ReadingListResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call,
                                 @NonNull Response<BaseJsonResponse<ReadingListResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Users updated successfully");
                } else {
                    String errorMessage = "Failed to update users";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<ReadingListResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Delete reading list
     */
    public void deleteReadingList(String listId, ApiCallback<Object> callback) {
        // listId from API response is already encoded, use directly
        Log.d(TAG, "Deleting reading list: " + listId);
        Call<BaseJsonResponse<Object>> call = readingListApi.deleteReadingList(listId);
        call.enqueue(new Callback<BaseJsonResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<Object>> call,
                                 @NonNull Response<BaseJsonResponse<Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                    Log.d(TAG, "Reading list deleted successfully");
                } else {
                    String errorMessage = "Failed to delete reading list";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}

