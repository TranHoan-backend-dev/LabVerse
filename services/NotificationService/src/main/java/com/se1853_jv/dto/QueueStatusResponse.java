package com.se1853_jv.dto;

import com.se1853_jv.model.NotificationQueue;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class QueueStatusResponse {
    private UUID queueId;
    private UUID targetUserId;
    private String title;
    private String message;
    private String linkTo;
    private NotificationQueue.QueueStatus status;
    private int retryCount;
    private String errorMessage;
    private Instant createdAt;
    private Instant processedAt;
}
