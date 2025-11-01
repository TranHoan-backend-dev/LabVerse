package com.se1853_jv.labverse.data.api.collection;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.Constants;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.CollectionRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionsPageResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CollectionApiHandler {
    private static final String TAG = "CollectionApiHandler";
    private final CollectionApi apiService;

    public CollectionApiHandler() {
        var gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();

        var logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        var client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        var retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiService = retrofit.create(CollectionApi.class);
    }

    public void getCollections(int page, int size, ApiCallback<CollectionsPageResponse> callback) {
        Log.d(TAG, "getCollections: page=" + page + ", size=" + size);
        Call<BaseJsonResponse<CollectionsPageResponse>> call = apiService.getCollections(page, size);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<CollectionsPageResponse>> call,
                                   @NonNull Response<BaseJsonResponse<CollectionsPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Collections fetched: " + (result != null && result.getContent() != null ? result.getContent().size() : 0));
                } else {
                    Log.e(TAG, "Server Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<CollectionsPageResponse>> call,
                                   @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void getCollectionById(String id, ApiCallback<CollectionResponse> callback) {
        Log.d(TAG, "getCollectionById: " + id);
        Call<BaseJsonResponse<CollectionResponse>> call = apiService.getCollectionById(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<CollectionResponse>> call,
                                   @NonNull Response<BaseJsonResponse<CollectionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Collection fetched: " + (result != null ? result.getName() : "null"));
                } else {
                    Log.e(TAG, "Server Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<CollectionResponse>> call,
                                   @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void createCollection(CollectionRequest request, ApiCallback<CollectionResponse> callback) {
        Log.d(TAG, "createCollection: " + request.getName());
        Call<BaseJsonResponse<CollectionResponse>> call = apiService.createCollection(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<CollectionResponse>> call,
                                   @NonNull Response<BaseJsonResponse<CollectionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Collection created: " + (result != null ? result.getName() : "null"));
                } else {
                    Log.e(TAG, "Server Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<CollectionResponse>> call,
                                   @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }
}


