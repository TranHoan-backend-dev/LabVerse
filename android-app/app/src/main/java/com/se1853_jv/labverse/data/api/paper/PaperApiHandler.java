package com.se1853_jv.labverse.data.api.paper;

import static com.se1853_jv.labverse.data.Constants.PAPER_ENDPOINT_URL;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.UploadPdfRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.ArrayList;
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

public class PaperApiHandler {
    private final PaperApi apiService;
    private final SessionManager sessionManager;

    public PaperApiHandler() {
        this(null);
    }

    public PaperApiHandler(Context context) {
        if (context != null) {
            this.sessionManager = new SessionManager(context);
        } else {
            this.sessionManager = null;
        }

        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add token interceptor if sessionManager is available
        Interceptor tokenInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder();
            
            if (sessionManager != null) {
                String token = sessionManager.getBearerToken();
                if (token != null) {
                    requestBuilder.header("Authorization", token);
                }
            }
            
            return chain.proceed(requestBuilder.build());
        };

        var clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging);
        
        if (sessionManager != null) {
            clientBuilder.addInterceptor(tokenInterceptor);
        }
        
        var client = clientBuilder.build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(PAPER_ENDPOINT_URL.concat("papers/"))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiService = retrofit.create(PaperApi.class);
    }

    public void getPaperDetails(String id, ApiCallback<PaperResearch> callback) {
        Log.d("PAPER_DATA", "getPaperDetails: " + id);
        Call<BaseJsonResponse<PaperResearch>> call = apiService.getPaperDetails(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<PaperResearch>> call, @NonNull Response<BaseJsonResponse<PaperResearch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d("PAPER_DATA", "PaperResearch: " + result.toString());
                } else {
                    Log.e("Server Error", "Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<PaperResearch>> call, @NonNull Throwable t) {
                Log.e("API Error", "Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void getCitationsOfPaper(String id, ApiCallback<List<Citation>> callback) {
        Log.d("CITATION_DATA", "getCitationsOfPaper: " + id);
        Call<BaseJsonResponse<List<Citation>>> call = apiService.getCitationOfPaper(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<List<Citation>>> call, @NonNull Response<BaseJsonResponse<List<Citation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Citation> citations = response.body().getData();

                    callback.onSuccess(new ArrayList<>(citations));
                } else {
                    Log.e("Server Error", "Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<List<Citation>>> call, @NonNull Throwable t) {
                Log.e("API Error", "Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAllPapers(String searchQuery, int currentPage, int pageSize, ApiCallback<List<PaperResearch>> callback) {
        Log.d("PAPER_DATA", "getAllPapers: searchQuery=" + searchQuery);
        
        Call<BaseJsonResponse<List<PaperResearch>>> call = apiService.getAllPapers(searchQuery, currentPage, pageSize);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<List<PaperResearch>>> call, @NonNull Response<BaseJsonResponse<List<PaperResearch>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result != null ? new ArrayList<>(result) : new ArrayList<>());
                    Log.d("PAPER_DATA", "Papers fetched: " + (result != null ? result.size() : 0));
                } else {
                    Log.e("Server Error", "Error: " + response.message());
                    if (response.errorBody() != null) {
                        try {
                            Log.e("Server Error", "Error body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("Server Error", "Could not read error body", e);
                        }
                    }
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<List<PaperResearch>>> call, @NonNull Throwable t) {
                Log.e("API Error", "Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void uploadPdf(UploadPdfRequest request, ApiCallback<Object> callback) {
        Log.d("PAPER_UPLOAD", "uploadPdf: " + request.getDataUrl());
        Call<BaseJsonResponse<Object>> call = apiService.uploadPdf(request);
        call.enqueue(new Callback<BaseJsonResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<Object>> call, @NonNull Response<BaseJsonResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("PAPER_UPLOAD", "Paper uploaded successfully");
                    callback.onSuccess(null);
                } else {
                    String errorMessage = "Failed to upload paper";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("PAPER_UPLOAD", "Error parsing error body", e);
                        }
                    }
                    Log.e("PAPER_UPLOAD", "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<Object>> call, @NonNull Throwable t) {
                Log.e("PAPER_UPLOAD", "API Error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}
