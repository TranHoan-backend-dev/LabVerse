package com.se1853_jv.service;

import com.se1853_jv.dto.NotificationRequestEvent;
import com.se1853_jv.dto.NotificationResponse;
import com.se1853_jv.dto.RegisterDeviceRequest;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void registerDevice(UUID userId, RegisterDeviceRequest request);
    List<NotificationResponse> getNotificationsForUser(UUID userId);
    void markAsRead(UUID userId, UUID notificationId);
    void processNewNotification(NotificationRequestEvent event);
}
