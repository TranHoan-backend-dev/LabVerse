package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Notification", indexes = {
        @Index(name = "idx_user_id_created_at", columnList = "userId, createdAt"),
        @Index(name = "idx_user_id_is_read", columnList = "userId, isRead")
})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    private UUID notificationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, length = 1024)
    private String message;

    @Column(name = "link_to", nullable = true, length = 255)
    private String linkTo;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.Instant createdAt;
}
