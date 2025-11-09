package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_devices", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"), // Đánh index để tìm theo userId
        @Index(name = "idx_fcm_token_unq", columnList = "fcmToken", unique = true) // Token là duy nhất
})
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_device_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "fcm_token", nullable = false, length = 512, unique = true)
    private String fcmToken; // FCM Token từ app Android

    @UpdateTimestamp // Tự động cập nhật khi có thay đổi
    @Column(name = "last_update_at")
    private Instant lastUpdatedAt;
}