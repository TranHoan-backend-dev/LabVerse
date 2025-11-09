package com.se1853_jv.service.Impl;

// ... imports (com.google.firebase.messaging.*)

import com.google.firebase.messaging.*;
import com.se1853_jv.service.FirebaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j // Dùng để log
public class FirebaseServiceImpl implements FirebaseService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendPushNotification(String fcmToken, String title, String message, Map<String, String> data) {
        // Cấu hình thông báo hiển thị
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build();

        // Cấu hình data payload (để app xử lý ngầm, chứa linkTo)
        Message fcmMessage = Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putAllData(data) // data chứa linkTo
                .setApnsConfig(ApnsConfig.builder() // Cấu hình cho iOS (nếu cần)
                        .setAps(Aps.builder().setSound("default").build())
                        .build())
                .build();

        try {
            // Gửi message
            String response = firebaseMessaging.send(fcmMessage);
            log.info("Successfully sent message to token {}: {}", fcmToken, response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send message to token {}: {}", fcmToken, e.getMessage());
        }
    }
}