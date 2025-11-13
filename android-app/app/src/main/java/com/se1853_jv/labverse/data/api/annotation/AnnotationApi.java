package com.se1853_jv.labverse.data.api.annotation;

import com.google.gson.annotations.SerializedName;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API interface cho Annotation endpoints
 * Base URL: http://10.0.2.2:8080/v1/api/annotations/
 */
public interface AnnotationApi {
    
    /**
     * Create a new note
     * POST /v1/api/annotations/notes
     */
    @POST("notes")
    Call<BaseJsonResponse<NoteResponse>> createNote(
        @Header("Authorization") String token,
        @Body CreateNoteRequest request
    );
    
    /**
     * Create a new highlight
     * POST /v1/api/annotations/highlights
     */
    @POST("highlights")
    Call<BaseJsonResponse<HighlightResponse>> createHighlight(
        @Header("Authorization") String token,
        @Body CreateHighlightRequest request
    );
    
    /**
     * Get all notes for a paper
     * GET /v1/api/annotations/notes?paperId=...&collectionId=...&userId=...
     */
    @GET("notes")
    Call<BaseJsonResponse<List<NoteResponse>>> getNotes(
        @Header("Authorization") String token,
        @Query("paperId") String paperId,
        @Query("collectionId") String collectionId,
        @Query("userId") String userId
    );
    
    /**
     * Get all highlights for a paper
     * GET /v1/api/annotations/highlights?paperId=...&collectionId=...&userId=...
     */
    @GET("highlights")
    Call<BaseJsonResponse<List<HighlightResponse>>> getHighlights(
        @Header("Authorization") String token,
        @Query("paperId") String paperId,
        @Query("collectionId") String collectionId,
        @Query("userId") String userId
    );
    
    /**
     * Delete a note
     * DELETE /v1/api/annotations/notes/{id}?paperId=...&collectionId=...
     */
    @DELETE("notes/{id}")
    Call<BaseJsonResponse<Void>> deleteNote(
        @Header("Authorization") String token,
        @Path("id") String noteId,
        @Query("paperId") String paperId,
        @Query("collectionId") String collectionId
    );
    
    /**
     * Delete a highlight
     * DELETE /v1/api/annotations/highlights/{id}?paperId=...&collectionId=...
     */
    @DELETE("highlights/{id}")
    Call<BaseJsonResponse<Void>> deleteHighlight(
        @Header("Authorization") String token,
        @Path("id") String highlightId,
        @Query("paperId") String paperId,
        @Query("collectionId") String collectionId
    );
    
    // DTOs
    class CreateNoteRequest {
        public String paperId;
        public String collectionId;
        public String content;
        public int coordinationX;
        public int coordinationY;
        public int pageNumber;
    }
    
    class CreateHighlightRequest {
        public String paperId;
        public String collectionId;
        public String color;
        public int coordinationX;
        public int coordinationY;
        public int pageNumber;
    }
    
    class NoteResponse {
        public String id;
        public String content;
        public int coordinationX;
        public int coordinationY;
        public int pageNumber;
    }
    
    class HighlightResponse {
        public String id;
        public String color;
        public int coordinationX;
        public int coordinationY;
        public int pageNumber;
    }

    /**
     * Export all annotations for a paper in a collection
     * GET /v1/api/annotations/export?paperId=...&collectionId=...
     */
    @GET("export")
    Call<BaseJsonResponse<ExportAnnotationsResponse>> exportAnnotations(
            @Header("Authorization") String authToken,
            @Query("paperId") String paperId,
            @Query("collectionId") String collectionId
    );

    /**
     * Danh sách bản export annotations
     */
    @GET("exports")
    Call<BaseJsonResponse<List<AnnotationExportSummaryResponse>>> listExports(
            @Header("Authorization") String authToken,
            @Query("paperId") String paperId,
            @Query("collectionId") String collectionId
    );

    /**
     * Lấy chi tiết một bản export
     */
    @GET("exports/{exportId}")
    Call<BaseJsonResponse<ExportAnnotationsResponse>> getExportDetail(
            @Header("Authorization") String authToken,
            @Path("exportId") String exportId
    );

    /**
     * Import annotations from exported data
     * POST /v1/api/annotations/import?paperId=...&collectionId=...
     */
    @POST("import")
    Call<BaseJsonResponse<Void>> importAnnotations(
            @Header("Authorization") String authToken,
            @Query("paperId") String paperId,
            @Query("collectionId") String collectionId,
            @Body ExportAnnotationsResponse importData
    );

    // --- Export/Import DTOs ---
    class ExportAnnotationsResponse {
        @SerializedName("paperId")
        public String paperId;
        
        @SerializedName("collectionId")
        public String collectionId;
        
        @SerializedName("exportedBy")
        public String exportedBy;
        
        @SerializedName("exportedAt")
        public String exportedAt;
        
        @SerializedName("notes")
        public List<NoteResponse> notes;
        
        @SerializedName("highlights")
        public List<HighlightResponse> highlights;
    }

    class AnnotationExportSummaryResponse {
        @SerializedName("exportId")
        public String exportId;
        @SerializedName("paperId")
        public String paperId;
        @SerializedName("collectionId")
        public String collectionId;
        @SerializedName("exportedBy")
        public String exportedBy;
        @SerializedName("exportedAt")
        public String exportedAt;
        @SerializedName("totalNotes")
        public Integer totalNotes;
        @SerializedName("totalHighlights")
        public Integer totalHighlights;
    }
}







