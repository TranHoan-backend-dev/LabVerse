package com.se1853_jv.labverse.presentation.notification;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.ApiCallback;
import com.se1853_jv.labverse.data.api.notification.NotificationApi;
import com.se1853_jv.labverse.data.api.notification.NotificationApiHandler;
import com.se1853_jv.labverse.data.utils.SessionManager;
import com.se1853_jv.labverse.presentation.common.HeaderHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationListActivity extends AppCompatActivity {
    private static final String TAG = "NotificationListActivity";
    
    private RecyclerView recyclerViewNotifications;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private NotificationAdapter adapter;
    private NotificationApiHandler apiHandler;
    private SessionManager sessionManager;
    private String jwtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_notification_list);
        
        // Get JWT token from SessionManager
        sessionManager = new SessionManager(this);
        jwtToken = sessionManager.getToken();
        
        if (jwtToken == null) {
            Log.w(TAG, "No JWT token found");
            finish();
            return;
        }
        
        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update badge when returning to this activity
        // This will be handled by the activity that has the header
    }

    private void initializeComponents() {
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        apiHandler = new NotificationApiHandler();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(new ArrayList<>(), notification -> {
            // When notification is clicked, mark as read
            if (notification.read == null || !notification.read) {
                markAsRead(notification.id);
            }
        });
        
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        apiHandler.getNotifications(jwtToken, new ApiCallback<List<NotificationApi.NotificationResponse>>() {
            @Override
            public void onSuccess(List<NotificationApi.NotificationResponse> notifications) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (notifications == null || notifications.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        adapter.setNotifications(new ArrayList<>());
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        // Sort by createdAt descending (newest first)
                        notifications.sort((a, b) -> {
                            if (a.createdAt == null || b.createdAt == null) return 0;
                            return b.createdAt.compareTo(a.createdAt);
                        });
                        adapter.setNotifications(notifications);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Không thể tải thông báo");
                    Log.e(TAG, "Error loading notifications: " + error);
                });
            }
        });
    }

    private void markAsRead(String notificationId) {
        apiHandler.markAsRead(jwtToken, notificationId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                runOnUiThread(() -> {
                    // Update notification in adapter
                    adapter.markAsRead(notificationId);
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error marking notification as read: " + error);
            }
        });
    }
}

