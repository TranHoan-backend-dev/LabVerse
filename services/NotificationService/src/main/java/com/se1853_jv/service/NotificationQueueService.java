package com.se1853_jv.service;

import com.se1853_jv.dto.NotificationRequestEvent;
import com.se1853_jv.dto.QueueStatusResponse;
import java.util.List;

public interface NotificationQueueService {
    /**
     * Thêm notification event vào queue
     */
    void enqueueNotification(NotificationRequestEvent event);

    /**
     * Xử lý các events trong queue
     */
    void processQueue();

    /**
     * Lấy tất cả queue items (dùng để test/debug)
     */
    List<QueueStatusResponse> getAllQueueItems();

    /**
     * Lấy queue items theo status (dùng để test/debug)
     */
    List<QueueStatusResponse> getQueueItemsByStatus(String status);
}
