package com.se1853_jv.labverse.data.api.collection;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.Constants;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.request.CollectionRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionPaperRequest;
import com.se1853_jv.labverse.data.dto.request.CollectionUserRequest;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionPaperDetailResponse;
import com.se1853_jv.labverse.data.dto.response.CollectionsPageResponse;

import java.util.List;

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
                .baseUrl(Constants.GROUP_ENDPOINT_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        apiService = retrofit.create(CollectionApi.class);
    }

    public void getMyCollections(String encodedUserId, ApiCallback<CollectionsPageResponse> callback) {
        Log.d(TAG, "getMyCollections: userId=" + encodedUserId);
        Call<BaseJsonResponse<CollectionsPageResponse>> call = apiService.getMyCollections(encodedUserId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<CollectionsPageResponse>> call,
                                   @NonNull Response<BaseJsonResponse<CollectionsPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "My collections fetched: " + (result != null && result.getContent() != null ? result.getContent().size() : 0));
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

    public void getSharedCollections(String encodedUserId, ApiCallback<CollectionsPageResponse> callback) {
        Log.d(TAG, "getSharedCollections: userId=" + encodedUserId);
        Call<BaseJsonResponse<CollectionsPageResponse>> call = apiService.getSharedCollections(encodedUserId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<CollectionsPageResponse>> call,
                                   @NonNull Response<BaseJsonResponse<CollectionsPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Shared collections fetched: " + (result != null && result.getContent() != null ? result.getContent().size() : 0));
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

    public void createCollection(@NonNull CollectionRequest request, ApiCallback<CollectionResponse> callback) {
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

    public void addPaperToCollection(@NonNull CollectionPaperRequest request, ApiCallback<CollectionPaperResponse> callback) {
        Log.d(TAG, "addPaperToCollection: collectionId=" + request.getCollectionId() + ", paperId=" + request.getPaperId());
        Call<BaseJsonResponse<CollectionPaperResponse>> call = apiService.addPaperToCollection(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<CollectionPaperResponse>> call,
                                   @NonNull Response<BaseJsonResponse<CollectionPaperResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Paper added to collection successfully");
                } else {
                    Log.e(TAG, "Server Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<CollectionPaperResponse>> call,
                                   @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void getPapersInCollection(String collectionId, ApiCallback<List<CollectionPaperDetailResponse>> callback) {
        Log.d(TAG, "getPapersInCollection: collectionId=" + collectionId);
        Call<BaseJsonResponse<List<CollectionPaperDetailResponse>>> call = apiService.getPapersInCollection(collectionId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<List<CollectionPaperDetailResponse>>> call,
                                   @NonNull Response<BaseJsonResponse<List<CollectionPaperDetailResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Papers in collection fetched: " + (result != null ? result.size() : 0));
                } else {
                    Log.e(TAG, "Server Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<List<CollectionPaperDetailResponse>>> call,
                                   @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void updatePaperStatus(@NonNull CollectionPaperRequest request, ApiCallback<CollectionPaperResponse> callback) {
        Log.d(TAG, "updatePaperStatus: collectionId=" + request.getCollectionId() + ", paperId=" + request.getPaperId());
        Call<BaseJsonResponse<CollectionPaperResponse>> call = apiService.updatePaperStatus(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<CollectionPaperResponse>> call,
                                   @NonNull Response<BaseJsonResponse<CollectionPaperResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Paper status updated successfully");
                } else {
                    Log.e(TAG, "Server Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<CollectionPaperResponse>> call,
                                   @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void addMemberToCollection(@NonNull CollectionUserRequest request, ApiCallback<Object> callback) {
        Log.d(TAG, "addMemberToCollection: collectionId=" + request.getCollectionId() + ", memberId=" + request.getMemberId());
        Call<BaseJsonResponse<Object>> call = apiService.addMemberToCollection(request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<Object>> call,
                                   @NonNull Response<BaseJsonResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Member added to collection successfully");
                } else {
                    Log.e(TAG, "Server Error: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<Object>> call,
                                   @NonNull Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }
}


