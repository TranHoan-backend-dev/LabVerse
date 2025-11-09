package com.se1853_jv.config;

import com.se1853_jv.service.NotificationQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task để tự động xử lý queue
 * Poll queue mỗi 5 giây một lần (có thể config trong application.properties)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.queue.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class QueueScheduler {

    private final NotificationQueueService queueService;

    /**
     * Xử lý queue mỗi 5 giây
     * Có thể config interval qua app.queue.scheduler.interval trong properties
     */
    @Scheduled(fixedDelayString = "${app.queue.scheduler.interval:5000}", initialDelay = 10000)
    public void processNotificationQueue() {
        try {
            queueService.processQueue();
        } catch (Exception e) {
            log.error("Error in scheduled queue processing: {}", e.getMessage(), e);
        }
    }
}
