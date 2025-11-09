package com.se1853_jv.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String message;
    private String linkTo;
    private boolean isRead;
    private Instant createdAt;
}