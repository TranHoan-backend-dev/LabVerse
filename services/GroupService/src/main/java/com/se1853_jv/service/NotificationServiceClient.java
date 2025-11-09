package com.se1853_jv.service;

import com.se1853_jv.dto.NotificationRequestEvent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE", path = "/v1/api/notifications")
public interface NotificationServiceClient {
    
    @PostMapping("/events")
    ResponseEntity<Void> createNotificationEvent(@RequestBody NotificationRequestEvent event);
}

