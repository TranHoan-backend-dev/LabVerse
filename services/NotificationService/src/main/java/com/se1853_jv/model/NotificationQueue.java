package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity để lưu các notification events trong queue
 * Dùng database-backed queue để xử lý notification async
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_queue", indexes = {
        @Index(name = "idx_queue_status_created", columnList = "status, createdAt")
})
public class NotificationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "queue_id")
    private UUID queueId;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 1024)
    private String message;

    @Column(name = "link_to", nullable = true)
    private String linkTo;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private QueueStatus status = QueueStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "error_message", length = 2048)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    public enum QueueStatus {
        PENDING,    // Chưa xử lý
        PROCESSING, // Đang xử lý
        COMPLETED,  // Đã xử lý xong
        FAILED      // Xử lý lỗi
    }
}
