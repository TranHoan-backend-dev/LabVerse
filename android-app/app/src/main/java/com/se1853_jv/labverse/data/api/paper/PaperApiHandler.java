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
import com.se1853_jv.labverse.data.dto.response.PapersPageResponse;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.util.ArrayList;
import java.util.List;

import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

        var logging = new HttpLoggingInterceptor(message -> Log.d("HTTP", message));
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

    public void getAllPapers(String searchQuery, int currentPage, int pageSize, 
                             String author, String journal, Integer yearFrom, Integer yearTo,
                             ApiCallback<List<PaperResearch>> callback) {
        Log.d("PAPER_DATA", "getAllPapers called with:");
        Log.d("PAPER_DATA", "  searchQuery=" + searchQuery);
        Log.d("PAPER_DATA", "  currentPage=" + currentPage);
        Log.d("PAPER_DATA", "  pageSize=" + pageSize);
        Log.d("PAPER_DATA", "  author=" + author);
        Log.d("PAPER_DATA", "  journal=" + journal);
        Log.d("PAPER_DATA", "  yearFrom=" + yearFrom);
        Log.d("PAPER_DATA", "  yearTo=" + yearTo);
        
        Call<BaseJsonResponse<PapersPageResponse>> call = apiService.getAllPapers(
                searchQuery, currentPage, pageSize, author, journal, yearFrom, yearTo);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<PapersPageResponse>> call, @NonNull Response<BaseJsonResponse<PapersPageResponse>> response) {
                Log.d("PAPER_DATA", "Response received:");
                Log.d("PAPER_DATA", "  isSuccessful: " + response.isSuccessful());
                Log.d("PAPER_DATA", "  code: " + response.code());
                Log.d("PAPER_DATA", "  message: " + response.message());
                
                if (response.isSuccessful()) {
                    BaseJsonResponse<PapersPageResponse> body = response.body();
                    if (body != null) {
                        Log.d("PAPER_DATA", "Response body:");
                        Log.d("PAPER_DATA", "  status: " + body.getStatus());
                        Log.d("PAPER_DATA", "  message: " + body.getMessage());
                        
                        PapersPageResponse pageResponse = body.getData();
                        Log.d("PAPER_DATA", "pageResponse is null: " + (pageResponse == null));
                        if (pageResponse != null) {
                            Log.d("PAPER_DATA", "pageResponse.getContent() is null: " + (pageResponse.getContent() == null));
                            if (pageResponse.getContent() != null) {
                                List<PaperResearch> result = pageResponse.getContent();
                                Log.d("PAPER_DATA", "Papers fetched: " + result.size());
                                Log.d("PAPER_DATA", "Total elements: " + pageResponse.getTotalElements());
                                Log.d("PAPER_DATA", "Total pages: " + pageResponse.getTotalPages());
                                callback.onSuccess(new ArrayList<>(result));
                            } else {
                                Log.w("PAPER_DATA", "Response content is null");
                                callback.onSuccess(new ArrayList<>());
                            }
                        } else {
                            Log.w("PAPER_DATA", "Response data (pageResponse) is null");
                            callback.onSuccess(new ArrayList<>());
                        }
                    } else {
                        Log.e("PAPER_DATA", "Response body is null");
                        callback.onError("Response body is null");
                    }
                } else {
                    Log.e("PAPER_DATA", "Response not successful. Code: " + response.code());
                    String errorMessage = "Server error: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("PAPER_DATA", "Error body: " + errorBody);
                            errorMessage += " - " + errorBody;
                        } catch (Exception e) {
                            Log.e("PAPER_DATA", "Could not read error body", e);
                        }
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<PapersPageResponse>> call, @NonNull Throwable t) {
                Log.e("PAPER_DATA", "API call failed", t);
                Log.e("PAPER_DATA", "Failure message: " + t.getMessage());
                if (t.getCause() != null) {
                    Log.e("PAPER_DATA", "Cause: " + t.getCause().getMessage());
                }
                callback.onError(t.getMessage());
            }
        });
    }

    public void getPapersByUserId(String userId, ApiCallback<List<PaperResearch>> callback) {
        Log.d("PAPER_DATA", "getPapersByUserId called with encoded userId: " + userId);
        Call<BaseJsonResponse<List<PaperResearch>>> call = apiService.getPapersByUserId(userId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<List<PaperResearch>>> call, @NonNull Response<BaseJsonResponse<List<PaperResearch>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body().getData();
                    callback.onSuccess(result != null ? new ArrayList<>(result) : new ArrayList<>());
                    Log.d("PAPER_DATA", "Papers fetched for user: " + (result != null ? result.size() : 0));
                } else {
                    String errorMessage = "Failed to get papers by user";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("PAPER_DATA", "Error parsing error body", e);
                        }
                    }
                    Log.e("PAPER_DATA", "Server Error: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<List<PaperResearch>>> call, @NonNull Throwable t) {
                Log.e("PAPER_DATA", "API Error: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }

    public void uploadPdf(@NonNull UploadPdfRequest request, String userId, ApiCallback<Object> callback) {
        Log.d("PAPER_UPLOAD", "uploadPdf: " + request.getDataUrl() + ", userId: " + userId);
        Call<BaseJsonResponse<Object>> call = apiService.uploadPdf(request, userId);
        call.enqueue(new Callback<>() {
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

    /**
     * Upload PDF file with metadata to backend
     * @param context Android context (needed for content:// URIs)
     * @param fileUri URI of the PDF file
     * @param title Paper title
     * @param authors Paper authors
     * @param journal Paper journal
     * @param publicationYear Publication year
     * @param doi DOI (optional, can be null)
     * @param description Description (optional, can be null)
     * @param keywords List of keywords (optional, can be null)
     * @param tags List of tags (optional, can be null)
     * @param userId User ID
     * @param callback Callback for result
     */
    public void uploadPdfWithFile(
            Context context,
            Uri fileUri,
            String title,
            String authors,
            String journal,
            int publicationYear,
            String doi,
            String description,
            List<String> keywords,
            List<String> tags,
            String userId,
            ApiCallback<Object> callback
    ) {
        Log.d("PAPER_UPLOAD", "uploadPdfWithFile: title=" + title + ", userId: " + userId);
        
        try {
            // Convert URI to File
            File file = getFileFromUri(context, fileUri);
            if (file == null || !file.exists()) {
                callback.onError("File does not exist or cannot be accessed");
                return;
            }
            
            // Create RequestBody for file
            RequestBody fileRequestBody = RequestBody.create(
                    MediaType.parse("application/pdf"),
                    file
            );
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileRequestBody);
            
            // Create RequestBody for metadata
            MediaType textPlain = MediaType.parse("text/plain");
            RequestBody titleBody = RequestBody.create(textPlain, title);
            RequestBody authorsBody = RequestBody.create(textPlain, authors);
            RequestBody journalBody = RequestBody.create(textPlain, journal);
            RequestBody publicationYearBody = RequestBody.create(textPlain, String.valueOf(publicationYear));
            
            // For optional fields, use empty string if null to avoid Retrofit issues with null RequestBody
            RequestBody doiBody = RequestBody.create(textPlain, (doi != null && !doi.isEmpty()) ? doi : "");
            RequestBody descriptionBody = RequestBody.create(textPlain, (description != null && !description.isEmpty()) ? description : "");
            RequestBody keywordsBody = RequestBody.create(textPlain, (keywords != null && !keywords.isEmpty()) 
                    ? String.join(",", keywords) : "");
            RequestBody tagsBody = RequestBody.create(textPlain, (tags != null && !tags.isEmpty()) 
                    ? String.join(",", tags) : "");
            
            // Call API
            Call<BaseJsonResponse<Object>> call = apiService.uploadPdfWithFile(
                    filePart,
                    titleBody,
                    authorsBody,
                    journalBody,
                    publicationYearBody,
                    doiBody,
                    descriptionBody,
                    keywordsBody,
                    tagsBody,
                    userId
            );
            
            call.enqueue(new Callback<>() {
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
        } catch (Exception e) {
            Log.e("PAPER_UPLOAD", "Error preparing file upload: " + e.getMessage(), e);
            callback.onError("Error preparing file upload: " + e.getMessage());
        }
    }
    
    /**
     * Convert URI to File, handling both file:// and content:// URIs
     */
    private File getFileFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        
        String scheme = uri.getScheme();
        if (scheme == null) {
            return null;
        }
        
        if ("file".equals(scheme)) {
            // File URI - return directly
            String path = uri.getPath();
            if (path != null) {
                return new File(path);
            }
        } else if ("content".equals(scheme)) {
            // Content URI - copy to temp file
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    return null;
                }
                
                File tempFile = new File(context.getCacheDir(), "upload_" + System.currentTimeMillis() + ".pdf");
                FileOutputStream outputStream = new FileOutputStream(tempFile);
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                outputStream.close();
                inputStream.close();
                
                return tempFile;
            } catch (IOException e) {
                Log.e("PAPER_UPLOAD", "Error copying content URI to file", e);
                return null;
            }
        }
        
        return null;
    }
}
