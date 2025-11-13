package com.se1853_jv.labverse.data.api.annotation;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.se1853_jv.labverse.data.Constants;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Handler để gọi Annotation API từ group-service
 */
public class AnnotationApiHandler {
    private static final String TAG = "AnnotationApiHandler";
    private static final String BASE_URL = Constants.GROUP_ENDPOINT_URL + "annotations/";
    private final AnnotationApi apiService;

    public AnnotationApiHandler() {
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
        
        apiService = retrofit.create(AnnotationApi.class);
    }

    /**
     * Create a note
     */
    public void createNote(String token, AnnotationApi.CreateNoteRequest request, ApiCallback<AnnotationApi.NoteResponse> callback) {
        Log.d(TAG, "Creating note for paper: " + request.paperId);
        
        Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.NoteResponse>> call = 
            apiService.createNote("Bearer " + token, request);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.NoteResponse>> call,
                                 @NonNull Response<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.NoteResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Note created successfully: " + result.id);
                } else {
                    Log.e(TAG, "Error creating note: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.NoteResponse>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "Failed to create note", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Create a highlight
     */
    public void createHighlight(String token, AnnotationApi.CreateHighlightRequest request, ApiCallback<AnnotationApi.HighlightResponse> callback) {
        Log.d(TAG, "Creating highlight for paper: " + request.paperId);
        
        Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.HighlightResponse>> call = 
            apiService.createHighlight("Bearer " + token, request);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.HighlightResponse>> call,
                                 @NonNull Response<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.HighlightResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Highlight created successfully: " + result.id);
                } else {
                    Log.e(TAG, "Error creating highlight: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<AnnotationApi.HighlightResponse>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "Failed to create highlight", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Get notes for a paper
     */
    public void getNotes(String token, String paperId, String collectionId, String userId, ApiCallback<List<AnnotationApi.NoteResponse>> callback) {
        Log.d(TAG, "Getting notes for paper: " + paperId);
        
        Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.NoteResponse>>> call = 
            apiService.getNotes("Bearer " + token, paperId, collectionId, userId);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.NoteResponse>>> call,
                                 @NonNull Response<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.NoteResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result != null ? new ArrayList<>(result) : new ArrayList<>());
                    Log.d(TAG, "Notes fetched: " + (result != null ? result.size() : 0));
                } else {
                    Log.e(TAG, "Error getting notes: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.NoteResponse>>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "Failed to get notes", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Get highlights for a paper
     */
    public void getHighlights(String token, String paperId, String collectionId, String userId, ApiCallback<List<AnnotationApi.HighlightResponse>> callback) {
        Log.d(TAG, "Getting highlights for paper: " + paperId);
        
        Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.HighlightResponse>>> call = 
            apiService.getHighlights("Bearer " + token, paperId, collectionId, userId);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.HighlightResponse>>> call,
                                 @NonNull Response<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.HighlightResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result != null ? new ArrayList<>(result) : new ArrayList<>());
                    Log.d(TAG, "Highlights fetched: " + (result != null ? result.size() : 0));
                } else {
                    Log.e(TAG, "Error getting highlights: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.se1853_jv.labverse.data.dto.response.BaseJsonResponse<List<AnnotationApi.HighlightResponse>>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "Failed to get highlights", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Export annotations for a paper in a collection
     */
    public void exportAnnotations(String token, String paperId, String collectionId, 
                                  ApiCallback<AnnotationApi.ExportAnnotationsResponse> callback) {
        Log.d(TAG, "Exporting annotations for paper: " + paperId);
        
        Call<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> call = 
            apiService.exportAnnotations("Bearer " + token, paperId, collectionId);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> call,
                                 @NonNull Response<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result);
                    Log.d(TAG, "Annotations exported successfully");
                } else {
                    Log.e(TAG, "Error exporting annotations: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "Failed to export annotations", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Import annotations from exported data
     */
    public void importAnnotations(String token, String paperId, String collectionId,
                                  AnnotationApi.ExportAnnotationsResponse importData,
                                  ApiCallback<Void> callback) {
        Log.d(TAG, "Importing annotations for paper: " + paperId);
        
        Call<BaseJsonResponse<Void>> call = 
            apiService.importAnnotations("Bearer " + token, paperId, collectionId, importData);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<Void>> call,
                                 @NonNull Response<BaseJsonResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                    Log.d(TAG, "Annotations imported successfully");
                } else {
                    Log.e(TAG, "Error importing annotations: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<Void>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "Failed to import annotations", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Load danh sách bản export annotations.
     */
    public void listExports(String token, String paperId, String collectionId,
                            ApiCallback<List<AnnotationApi.AnnotationExportSummaryResponse>> callback) {
        Log.d(TAG, "Listing annotation exports for paper=" + paperId + ", collection=" + collectionId);

        Call<BaseJsonResponse<List<AnnotationApi.AnnotationExportSummaryResponse>>> call =
                apiService.listExports("Bearer " + token, paperId, collectionId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<List<AnnotationApi.AnnotationExportSummaryResponse>>> call,
                                   @NonNull Response<BaseJsonResponse<List<AnnotationApi.AnnotationExportSummaryResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<List<AnnotationApi.AnnotationExportSummaryResponse>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Failed to list exports", t);
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Lấy chi tiết một bản export.
     */
    public void getExportDetail(String token, String exportId,
                                ApiCallback<AnnotationApi.ExportAnnotationsResponse> callback) {
        Log.d(TAG, "Getting export detail for id=" + exportId);

        Call<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> call =
                apiService.getExportDetail("Bearer " + token, exportId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> call,
                                   @NonNull Response<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<AnnotationApi.ExportAnnotationsResponse>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "Failed to get export detail", t);
                callback.onError(t.getMessage());
            }
        });
    }
}







