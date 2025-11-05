package com.se1853_jv.labverse.data.api.paper;

import static com.se1853_jv.labverse.data.Constants.PAPER_ENDPOINT_URL;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaperApiHandler {
    private final PaperApi apiService;

    public PaperApiHandler() {
        // Shared Gson and OkHttpClient để reuse
        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        var client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        // Retrofit instance 1 cho PAPER_ENDPOINT_URL
        var retrofit = new Retrofit.Builder()
                .baseUrl(PAPER_ENDPOINT_URL.concat("/papers"))
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
}
