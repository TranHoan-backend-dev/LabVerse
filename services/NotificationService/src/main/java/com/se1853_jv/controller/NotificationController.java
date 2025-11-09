package com.se1853_jv.controller;

import com.se1853_jv.dto.NotificationRequestEvent;
import com.se1853_jv.dto.NotificationResponse;
import com.se1853_jv.dto.QueueStatusResponse;
import com.se1853_jv.dto.RegisterDeviceRequest;
import com.se1853_jv.service.NotificationQueueService;
import com.se1853_jv.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/v1/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationQueueService queueService;

    //Đăng ký device token
    @PostMapping("/devices")
    public ResponseEntity<Void> registerDevice(
            @RequestBody RegisterDeviceRequest request,
            Authentication authentication // Spring Security sẽ inject cái này
    ) {
        // Bạn cần 1 hàm helper để lấy UUID từ JWT
        UUID userId = getUserIdFromAuthentication(authentication);
        notificationService.registerDevice(userId, request);
        return ResponseEntity.ok().build();
    }

    //Lấy lịch sử thông báo
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    //Đánh dấu đã đọc
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") UUID notificationId,
            Authentication authentication
    ) {
        UUID userId = getUserIdFromAuthentication(authentication);
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    /**
     * REST endpoint để các service khác gửi notification events
     * Các service khác sẽ gọi endpoint này để gửi notification
     */
    @PostMapping("/events")
    public ResponseEntity<Void> createNotificationEvent(
            @RequestBody NotificationRequestEvent event
    ) {
        // Thêm vào queue để xử lý async
        queueService.enqueueNotification(event);
        return ResponseEntity.accepted().build(); // 202 Accepted - đã nhận và sẽ xử lý
    }

    /**
     * Endpoint test đơn giản để kiểm tra service đang chạy
     */
    @GetMapping("/queue/test")
    public ResponseEntity<String> testQueueEndpoint() {
        return ResponseEntity.ok("Queue endpoint is working!");
    }

    /**
     * Endpoint để xem queue status (dùng để test/debug)
     * GET /v1/api/notifications/queue?status=PENDING (optional query param)
     */
    @GetMapping("/queue")
    public ResponseEntity<List<QueueStatusResponse>> getQueueStatus(
            @RequestParam(required = false) String status
    ) {
        List<QueueStatusResponse> items;
        if (status != null && !status.isEmpty()) {
            items = queueService.getQueueItemsByStatus(status);
        } else {
            items = queueService.getAllQueueItems();
        }
        return ResponseEntity.ok(items);
    }


    /**
     * Helper method để lấy userId từ Authentication
     * AccountService trả về String ID (UUID dạng string), cần convert sang UUID
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new SecurityException("User not authenticated");
        }
        try {
            // AccountService lưu userId (String) trong JWT subject
            // Convert String ID sang UUID
            String userIdString = authentication.getName();
            return UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Invalid user ID format in token: " + authentication.getName());
        }
    }
}