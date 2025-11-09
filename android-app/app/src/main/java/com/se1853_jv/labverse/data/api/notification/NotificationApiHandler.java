package com.se1853_jv.labverse.data.api.notification;

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
 * Handler để gọi Notification API từ NotificationService
 */
public class NotificationApiHandler {
    private static final String TAG = "NotificationApiHandler";
    private static final String BASE_URL = Constants.NOTIFICATION_ENDPOINT_URL;
    private final NotificationApi apiService;

    public NotificationApiHandler() {
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
        
        apiService = retrofit.create(NotificationApi.class);
    }

    /**
     * Get all notifications for the current user
     * Note: NotificationService returns array directly, not wrapped in BaseJsonResponse
     */
    public void getNotifications(String token, ApiCallback<List<NotificationApi.NotificationResponse>> callback) {
        Log.d(TAG, "Getting notifications");
        
        Call<List<NotificationApi.NotificationResponse>> call = 
            apiService.getNotifications("Bearer " + token);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificationApi.NotificationResponse>> call,
                                 @NonNull Response<List<NotificationApi.NotificationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var result = response.body();
                    callback.onSuccess(result != null ? new ArrayList<>(result) : new ArrayList<>());
                    Log.d(TAG, "Notifications fetched: " + (result != null ? result.size() : 0));
                } else {
                    Log.e(TAG, "Error getting notifications: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificationApi.NotificationResponse>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "API call failed for getNotifications: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String token, String notificationId, ApiCallback<Void> callback) {
        Log.d(TAG, "Marking notification as read: " + notificationId);
        
        Call<BaseJsonResponse<Void>> call = 
            apiService.markAsRead("Bearer " + token, notificationId);
        
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<BaseJsonResponse<Void>> call,
                                 @NonNull Response<BaseJsonResponse<Void>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                    Log.d(TAG, "Notification marked as read: " + notificationId);
                } else {
                    Log.e(TAG, "Error marking notification as read: " + response.message());
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseJsonResponse<Void>> call,
                                @NonNull Throwable t) {
                Log.e(TAG, "API call failed for markAsRead: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }
}

