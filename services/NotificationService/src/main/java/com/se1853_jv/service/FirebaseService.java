package com.se1853_jv.service;

import java.util.Map;

public interface FirebaseService {
    void sendPushNotification(String fcmToken, String title, String message, Map<String, String> data);

}
