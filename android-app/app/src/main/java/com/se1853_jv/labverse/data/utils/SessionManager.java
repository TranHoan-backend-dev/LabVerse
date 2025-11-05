package com.se1853_jv.labverse.data.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.se1853_jv.labverse.data.dto.response.AuthResponse;

/**
 * SessionManager để lưu trữ và quản lý thông tin đăng nhập của user
 */
public class SessionManager {
    private static final String PREF_NAME = "LabVerseSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_AVATAR_URL = "avatarUrl";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Lưu thông tin đăng nhập
     */
    public void saveAuthResponse(AuthResponse authResponse) {
        editor.putString(KEY_TOKEN, authResponse.getToken());
        editor.putString(KEY_USER_ID, authResponse.getUserId());
        editor.putString(KEY_EMAIL, authResponse.getEmail());
        editor.putString(KEY_USERNAME, authResponse.getUsername());
        editor.putString(KEY_FULL_NAME, authResponse.getFullName());
        editor.putString(KEY_AVATAR_URL, authResponse.getAvatarUrl());
        editor.putString(KEY_ROLE, authResponse.getRole());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Lấy token
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * Lấy Bearer token (với prefix "Bearer ")
     */
    public String getBearerToken() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

    /**
     * Lấy User ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Lấy email
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * Lấy username
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    /**
     * Lấy full name
     */
    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    /**
     * Lấy avatar URL
     */
    public String getAvatarUrl() {
        return prefs.getString(KEY_AVATAR_URL, null);
    }

    /**
     * Lấy role
     */
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Đăng xuất - xóa tất cả session data
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Cập nhật avatar URL
     */
    public void updateAvatarUrl(String avatarUrl) {
        editor.putString(KEY_AVATAR_URL, avatarUrl);
        editor.apply();
    }

    /**
     * Cập nhật full name
     */
    public void updateFullName(String fullName) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }
}

