package com.se1853_jv.service.Impl;

import com.se1853_jv.dto.NotificationRequestEvent;
import com.se1853_jv.dto.NotificationResponse;
import com.se1853_jv.dto.RegisterDeviceRequest;
import com.se1853_jv.model.Notification;
import com.se1853_jv.model.UserDevice;
import com.se1853_jv.repository.NotificationRepository;
import com.se1853_jv.repository.UserDeviceRepository;
import com.se1853_jv.service.FirebaseService;
import com.se1853_jv.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FirebaseService firebaseService;

    // 1. Logic cho app Android: Đăng ký device token
    @Transactional
    public void registerDevice(UUID userId, RegisterDeviceRequest request) {
        userDeviceRepository.findByFcmToken(request.getFcmToken())
                .ifPresentOrElse(
                        // Nếu token đã tồn tại
                        device -> {
                            device.setUserId(userId); // Cập nhật lại userId (nếu user đăng nhập lại)
                            userDeviceRepository.save(device);
                            log.info("Updated device token for user {}", userId);
                        },
                        // Nếu token chưa tồn tại
                        () -> {
                            UserDevice newDevice = UserDevice.builder()
                                    .userId(userId)
                                    .fcmToken(request.getFcmToken())
                                    .build();
                            userDeviceRepository.save(newDevice);
                            log.info("Registered new device token for user {}", userId);
                        }
                );
    }

    // 2. Logic cho app Android: Lấy lịch sử thông báo
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse) // Chuyển Entity -> DTO
                .collect(Collectors.toList());
    }

    // 3. Logic cho app Android: Đánh dấu đã đọc
    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found")); // Cần tạo Exception custom

        // Bảo mật: Chỉ user sở hữu mới được đánh dấu đã đọc
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized"); // Cần tạo Exception custom
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // 4. Logic xử lý sự kiện thông báo mới (từ database-backed queue)
    @Transactional
    public void processNewNotification(NotificationRequestEvent event) {
        log.info("Processing new notification for user {}", event.getTargetUserId());

        // a. Lưu thông báo vào CSDL
        Notification notification = Notification.builder()
                .userId(event.getTargetUserId())
                .title(event.getTitle())
                .message(event.getMessage())
                .linkTo(event.getLinkTo())
                .isRead(false)
                .build();
        notificationRepository.save(notification);

        // b. Lấy tất cả device token của user đó
        List<UserDevice> devices = userDeviceRepository.findByUserId(event.getTargetUserId());

        if (devices.isEmpty()) {
            log.warn("No devices found for user {}", event.getTargetUserId());
            return;
        }

        // c. Gửi push notification đến tất cả thiết bị
        Map<String, String> data = event.getLinkTo() != null
                ? Map.of("linkTo", event.getLinkTo())
                : Collections.emptyMap();

        for (UserDevice device : devices) {
            firebaseService.sendPushNotification(
                    device.getFcmToken(),
                    event.getTitle(),
                    event.getMessage(),
                    data
            );
        }
    }

    // Hàm helper để map Entity sang DTO
    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getNotificationId())
                .title(n.getTitle())
                .message(n.getMessage())
                .linkTo(n.getLinkTo())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
