package com.se1853_jv.labverse.presentation.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.data.api.notification.NotificationApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationApi.NotificationResponse> notifications;
    private OnNotificationClickListener listener;
    private java.util.Set<String> expandedItems = new java.util.HashSet<>();

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationApi.NotificationResponse notification);
    }

    public NotificationAdapter(List<NotificationApi.NotificationResponse> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.listener = listener;
    }

    public void setNotifications(List<NotificationApi.NotificationResponse> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        expandedItems.clear();
        notifyDataSetChanged();
    }

    public void markAsRead(String notificationId) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).id.equals(notificationId)) {
                notifications.get(i).read = true;
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationApi.NotificationResponse notification = notifications.get(position);
        boolean isExpanded = expandedItems.contains(notification.id);
        holder.bind(notification, listener, isExpanded, () -> {
            // Toggle expanded state
            if (expandedItems.contains(notification.id)) {
                expandedItems.remove(notification.id);
            } else {
                expandedItems.add(notification.id);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTimestamp;
        private final View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(NotificationApi.NotificationResponse notification, 
                        OnNotificationClickListener listener, 
                        boolean isExpanded,
                        Runnable onToggleExpand) {
            tvTitle.setText(notification.title);
            
            // Show/hide unread indicator
            if (notification.read != null && notification.read) {
                unreadIndicator.setVisibility(View.GONE);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
            }

            // Set message
            if (notification.message != null) {
                tvMessage.setText(notification.message);
            }

            // Set timestamp
            if (notification.createdAt != null) {
                String timeAgo = formatTimeAgo(notification.createdAt);
                tvTimestamp.setText(timeAgo);
            }

            // Show/hide message and timestamp based on expanded state
            tvMessage.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            tvTimestamp.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Click listener to expand/collapse
            itemView.setOnClickListener(v -> {
                onToggleExpand.run();
                
                // Notify listener
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        private String formatTimeAgo(String createdAt) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(createdAt);
                
                if (date == null) {
                    return "";
                }
                
                long now = System.currentTimeMillis();
                long diff = now - date.getTime();
                
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                
                if (days > 0) {
                    return days + " ngày trước";
                } else if (hours > 0) {
                    return hours + " giờ trước";
                } else if (minutes > 0) {
                    return minutes + " phút trước";
                } else {
                    return "Vừa xong";
                }
            } catch (ParseException e) {
                return "";
            }
        }
    }
}

