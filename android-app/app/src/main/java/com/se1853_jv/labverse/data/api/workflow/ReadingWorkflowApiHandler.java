package com.se1853_jv.labverse.data.api.workflow;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.ReadingWorkflowProgressRequest;
import com.se1853_jv.labverse.data.dto.request.ReadingWorkflowStatusRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.utils.EncoderUtils;
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

public class ReadingWorkflowApiHandler {
    private static final String TAG = "ReadingWorkflowApiHandler";
    private static final String BASE_URL = "http://10.0.2.2:8080/reading-service/";

    private final ReadingWorkflowApi readingWorkflowApi;
    private final SessionManager sessionManager;

    public ReadingWorkflowApiHandler(Context context) {
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
                .build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        readingWorkflowApi = retrofit.create(ReadingWorkflowApi.class);
    }

    /**
     * Update reading progress
     */
    public void updateProgress(ReadingWorkflowProgressRequest request, ApiCallback<String> callback) {
        // Encode IDs
        request.setCollectionId(EncoderUtils.encode(request.getCollectionId()));
        request.setPaperId(EncoderUtils.encode(request.getPaperId()));
        request.setUsersid(EncoderUtils.encode(request.getUsersid()));

        Log.d(TAG, "Updating reading progress: paperId=" + request.getPaperId() + 
                ", progress=" + request.getProgress() + "%, lastPage=" + request.getLastPage());

        Call<BaseJsonResponse<String>> call = readingWorkflowApi.updateProgress(request);
        call.enqueue(new Callback<BaseJsonResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<String>> call,
                                   @NonNull Response<BaseJsonResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Reading progress updated successfully");
                } else {
                    String errorMessage = "Failed to update reading progress";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<String>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Update reading status
     */
    public void updateStatus(ReadingWorkflowStatusRequest request, ApiCallback<String> callback) {
        // Encode IDs
        request.setCollectionId(EncoderUtils.encode(request.getCollectionId()));
        request.setPaperId(EncoderUtils.encode(request.getPaperId()));
        request.setUsersid(EncoderUtils.encode(request.getUsersid()));

        Log.d(TAG, "Updating reading status: paperId=" + request.getPaperId() + 
                ", status=" + request.getStatus());

        Call<BaseJsonResponse<String>> call = readingWorkflowApi.updateStatus(request);
        call.enqueue(new Callback<BaseJsonResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<String>> call,
                                   @NonNull Response<BaseJsonResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Reading status updated successfully");
                } else {
                    String errorMessage = "Failed to update reading status";
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
            public void onFailure(@NonNull Call<BaseJsonResponse<String>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }
}

