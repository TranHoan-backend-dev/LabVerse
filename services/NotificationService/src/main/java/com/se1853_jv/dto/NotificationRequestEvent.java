package com.se1853_jv.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class NotificationRequestEvent {
    private UUID targetUserId;
    private String title;
    private String message;
    private String linkTo;
}