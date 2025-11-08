package com.se1853_jv.labverse.presentation.common;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.se1853_jv.labverse.R;
import com.se1853_jv.labverse.presentation.profile.ProfileActivity;
import com.se1853_jv.labverse.presentation.team.TeamListActivity;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu;

/**
 * Helper class to setup header and navigation components across activities
 */
public class HeaderHelper {

    /**
     * Setup avatar click listener to navigate to ProfileActivity
     * @param activity The activity that contains the header
     */
//    public static void setupAvatarClickListener(AppCompatActivity activity) {
//        View headerView = activity.findViewById(R.id.header);
//        ImageView avatar = null;
//
//        if (headerView != null) {
//            // If header is included with id="header", find avatar in the included view
//            avatar = headerView.findViewById(R.id.avatar);
//        } else {
//            // If header is directly in the layout, find it directly
//            avatar = activity.findViewById(R.id.avatar);
//        }
//
//        if (avatar != null) {
//            avatar.setOnClickListener(v -> {
//                Intent intent = new Intent(activity, ProfileActivity.class);
//                activity.startActivity(intent);
//            });
//        }
//    }
//
//    /**
//     * Setup avatar click listener with a custom view root
//     * Useful when header is included but doesn't have an ID
//     * @param activity The activity
//     * @param rootView The root view that contains the header (usually activity root)
//     */
//    public static void setupAvatarClickListener(AppCompatActivity activity, View rootView) {
//        ImageView avatar = rootView.findViewById(R.id.avatar);
//        if (avatar != null) {
//            avatar.setOnClickListener(v -> {
//                Intent intent = new Intent(activity, ProfileActivity.class);
//                activity.startActivity(intent);
//            });
//        }
//    }

    /**
     * Setup avatar click listener to show a dropdown menu
     * @param activity The activity that contains the header
     */
    public static void setupAvatarClickListener(@NonNull AppCompatActivity activity) {
        View headerView = activity.findViewById(R.id.header);
        ImageView avatar = null;

        if (headerView != null) {
            avatar = headerView.findViewById(R.id.avatar);
        } else {
            avatar = activity.findViewById(R.id.avatar);
        }

        if (avatar != null) {
            // Gọi hàm setupMenu mới
            setupMenu(activity, avatar);
        }
    }

    /**
     * Setup avatar click listener with a custom view root
     * @param activity The activity
     * @param rootView The root view that contains the header
     */
    public static void setupAvatarClickListener(AppCompatActivity activity, @NonNull View rootView) {
        ImageView avatar = rootView.findViewById(R.id.avatar);
        if (avatar != null) {
            // Gọi hàm setupMenu mới
            setupMenu(activity, avatar);
        }
    }

    /**
     * (HÀM MỚI) Tạo và hiển thị PopupMenu khi click avatar
     */
    private static void setupMenu(AppCompatActivity activity, @NonNull ImageView avatar) {
        avatar.setOnClickListener(v -> {
            // Tạo PopupMenu
            PopupMenu popup = new PopupMenu(activity, v); // v là ImageView avatar

            // Gắn file menu (R.menu.avatar_menu)
            popup.getMenuInflater().inflate(R.menu.menu_avatar_dropdown, popup.getMenu());

            // Thiết lập sự kiện khi một mục menu được chọn
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.action_profile) {
                    // Chuyển đến Trang cá nhân
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    activity.startActivity(intent);
                    return true;

                } else if (itemId == R.id.action_logout) {
                    // Gọi hàm đăng xuất
                    logoutUser(activity);
                    return true;
                }

                return false;
            });

            // Hiển thị menu
            popup.show();
        });
    }

    /**
     * (HÀM MỚI) Xử lý logic đăng xuất
     */
    private static void logoutUser(AppCompatActivity activity) {
        // TODO: Viết logic đăng xuất đầy đủ ở đây
        // Ví dụ: Xóa SharedPreferences, xóa token, xóa database...

        Toast.makeText(activity, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        // Điều hướng người dùng về màn hình Đăng nhập
        // Intent intent = new Intent(activity, LoginActivity.class);
        // Đặt cờ để xóa hết các Activity cũ khỏi back stack
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // activity.startActivity(intent);
        // activity.finish();
    }
    /**
     * Setup profile navigation click listener in bottom navigation
     * @param activity The activity that contains the bottom navigation
     */
    public static void setupProfileNavigationClickListener(AppCompatActivity activity) {
        View bottomNavView = activity.findViewById(R.id.bottomNav);
        LinearLayout navProfile = null;

        if (bottomNavView != null) {
            // If bottom nav is included with id="bottomNav", find nav_profile in the included view
            navProfile = bottomNavView.findViewById(R.id.nav_profile);
        } else {
            // If bottom nav is directly in the layout, find it directly
            navProfile = activity.findViewById(R.id.nav_profile);
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(activity, ProfileActivity.class);
                activity.startActivity(intent);
            });
        }
    }

    /**
     * Setup both avatar and profile navigation click listeners
     * @param activity The activity
     */
    public static void setupProfileClickListeners(AppCompatActivity activity) {
        setupAvatarClickListener(activity);
        setupProfileNavigationClickListener(activity);
    }

    /**
     * Setup notification button click listener to navigate to NotificationListActivity
     * @param activity The activity that contains the header
     */
    public static void setupNotificationClickListener(@NonNull AppCompatActivity activity) {
        View headerView = activity.findViewById(R.id.header);
        android.widget.ImageButton btnNotification = null;
        android.view.View notificationContainer = null;

        if (headerView != null) {
            notificationContainer = headerView.findViewById(R.id.notification_container);
            btnNotification = headerView.findViewById(R.id.btn_notification);
        } else {
            notificationContainer = activity.findViewById(R.id.notification_container);
            btnNotification = activity.findViewById(R.id.btn_notification);
        }

        if (btnNotification != null) {
            // Make button clickable
            btnNotification.setClickable(true);
            btnNotification.setFocusable(true);
            
            btnNotification.setOnClickListener(v -> {
                android.util.Log.d("HeaderHelper", "Notification button clicked");
                Intent intent = new Intent(activity, com.se1853_jv.labverse.presentation.notification.NotificationListActivity.class);
                activity.startActivity(intent);
            });
            
            // Also setup click listener on container if exists
            if (notificationContainer != null) {
                notificationContainer.setOnClickListener(v -> {
                    android.util.Log.d("HeaderHelper", "Notification container clicked");
                    Intent intent = new Intent(activity, com.se1853_jv.labverse.presentation.notification.NotificationListActivity.class);
                    activity.startActivity(intent);
                });
            }
        } else {
            android.util.Log.w("HeaderHelper", "Notification button not found");
        }
    }

    /**
     * Setup notification button click listener with a custom view root
     * @param activity The activity
     * @param rootView The root view that contains the header
     */
    public static void setupNotificationClickListener(AppCompatActivity activity, @NonNull View rootView) {
        android.view.View notificationContainer = rootView.findViewById(R.id.notification_container);
        android.widget.ImageButton btnNotification = rootView.findViewById(R.id.btn_notification);
        
        if (btnNotification != null) {
            // Make button clickable
            btnNotification.setClickable(true);
            btnNotification.setFocusable(true);
            
            btnNotification.setOnClickListener(v -> {
                android.util.Log.d("HeaderHelper", "Notification button clicked");
                Intent intent = new Intent(activity, com.se1853_jv.labverse.presentation.notification.NotificationListActivity.class);
                activity.startActivity(intent);
            });
            
            // Also setup click listener on container if exists
            if (notificationContainer != null) {
                notificationContainer.setOnClickListener(v -> {
                    android.util.Log.d("HeaderHelper", "Notification container clicked");
                    Intent intent = new Intent(activity, com.se1853_jv.labverse.presentation.notification.NotificationListActivity.class);
                    activity.startActivity(intent);
                });
            }
        } else {
            android.util.Log.w("HeaderHelper", "Notification button not found in rootView");
        }
    }

    /**
     * Update notification badge count
     * @param activity The activity that contains the header
     * @param unreadCount Number of unread notifications
     */
    public static void updateNotificationBadge(@NonNull AppCompatActivity activity, int unreadCount) {
        View headerView = activity.findViewById(R.id.header);
        android.widget.TextView badge = null;

        if (headerView != null) {
            badge = headerView.findViewById(R.id.notification_badge);
        } else {
            badge = activity.findViewById(R.id.notification_badge);
        }

        if (badge != null) {
            if (unreadCount > 0) {
                badge.setVisibility(android.view.View.VISIBLE);
                if (unreadCount > 99) {
                    badge.setText("99+");
                } else {
                    badge.setText(String.valueOf(unreadCount));
                }
            } else {
                badge.setVisibility(android.view.View.GONE);
            }
        }
    }

    /**
     * Update notification badge count with a custom view root
     * @param rootView The root view that contains the header
     * @param unreadCount Number of unread notifications
     */
    public static void updateNotificationBadge(@NonNull View rootView, int unreadCount) {
        android.widget.TextView badge = rootView.findViewById(R.id.notification_badge);
        if (badge != null) {
            if (unreadCount > 0) {
                badge.setVisibility(android.view.View.VISIBLE);
                if (unreadCount > 99) {
                    badge.setText("99+");
                } else {
                    badge.setText(String.valueOf(unreadCount));
                }
            } else {
                badge.setVisibility(android.view.View.GONE);
            }
        }
    }

    /**
     * Load and update notification badge count
     * @param activity The activity that contains the header
     */
    public static void loadNotificationBadge(@NonNull AppCompatActivity activity) {
        com.se1853_jv.labverse.data.utils.SessionManager sessionManager = 
            new com.se1853_jv.labverse.data.utils.SessionManager(activity);
        String jwtToken = sessionManager.getToken();
        
        if (jwtToken == null) {
            updateNotificationBadge(activity, 0);
            return;
        }
        
        com.se1853_jv.labverse.data.api.notification.NotificationApiHandler apiHandler = 
            new com.se1853_jv.labverse.data.api.notification.NotificationApiHandler();
        
        apiHandler.getNotifications(jwtToken, new com.se1853_jv.labverse.data.api.ApiCallback<java.util.List<com.se1853_jv.labverse.data.api.notification.NotificationApi.NotificationResponse>>() {
            @Override
            public void onSuccess(java.util.List<com.se1853_jv.labverse.data.api.notification.NotificationApi.NotificationResponse> notifications) {
                activity.runOnUiThread(() -> {
                    if (notifications == null) {
                        updateNotificationBadge(activity, 0);
                        return;
                    }
                    
                    // Count unread notifications
                    int unreadCount = 0;
                    for (com.se1853_jv.labverse.data.api.notification.NotificationApi.NotificationResponse notification : notifications) {
                        if (notification.read == null || !notification.read) {
                            unreadCount++;
                        }
                    }
                    
                    updateNotificationBadge(activity, unreadCount);
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("HeaderHelper", "Error loading notification badge: " + error);
                activity.runOnUiThread(() -> updateNotificationBadge(activity, 0));
            }
        });
    }

    /**
     * Setup Lists navigation click listener in bottom navigation
     * @param activity The activity that contains the bottom navigation
     */
    public static void setupListsNavigationClickListener(AppCompatActivity activity) {
        View bottomNavView = activity.findViewById(R.id.bottomNav);
        LinearLayout navLists = null;

        if (bottomNavView != null) {
            // If bottom nav is included with id="bottomNav", find nav_lists in the included view
            navLists = bottomNavView.findViewById(R.id.nav_lists);
        } else {
            // If bottom nav is directly in the layout, find it directly
            navLists = activity.findViewById(R.id.nav_lists);
        }

        if (navLists != null) {
            navLists.setOnClickListener(v -> {
                Intent intent = new Intent(activity, TeamListActivity.class);
                activity.startActivity(intent);
            });
        }
    }
}

