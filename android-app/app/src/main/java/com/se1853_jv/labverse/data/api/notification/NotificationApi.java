package com.se1853_jv.labverse.data.api.notification;

import com.google.gson.annotations.SerializedName;
import com.se1853_jv.labverse.data.dto.response.BaseJsonResponse;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationApi {

    /**
     * Get all notifications for the current user
     * GET /v1/api/notifications
     * Note: NotificationService returns array directly, not wrapped in BaseJsonResponse
     */
    @GET("notifications")
    Call<List<NotificationResponse>> getNotifications(
            @Header("Authorization") String authToken
    );

    /**
     * Mark notification as read
     * PUT /v1/api/notifications/{id}/read
     */
    @PUT("notifications/{id}/read")
    Call<BaseJsonResponse<Void>> markAsRead(
            @Header("Authorization") String authToken,
            @Path("id") String notificationId
    );

    // --- Response DTOs ---
    class NotificationResponse {
        @SerializedName("id")
        public String id;
        
        @SerializedName("title")
        public String title;
        
        @SerializedName("message")
        public String message;
        
        @SerializedName("linkTo")
        public String linkTo;
        
        @SerializedName("read")
        public Boolean read; // Backend uses "read" not "isRead"
        
        @SerializedName("createdAt")
        public String createdAt;
    }
}

