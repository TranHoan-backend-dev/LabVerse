package com.se1853_jv.service.Impl;

import com.se1853_jv.dto.NotificationRequestEvent;
import com.se1853_jv.dto.QueueStatusResponse;
import com.se1853_jv.model.NotificationQueue;
import com.se1853_jv.repository.NotificationQueueRepository;
import com.se1853_jv.service.NotificationQueueService;
import com.se1853_jv.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationQueueServiceImpl implements NotificationQueueService {

    private final NotificationQueueRepository queueRepository;
    private final NotificationService notificationService;

    @Value("${app.queue.batch-size:10}")
    private int batchSize;

    /**
     * Thêm notification event vào queue
     */
    @Override
    @Transactional
    public void enqueueNotification(NotificationRequestEvent event) {
        NotificationQueue queueItem = NotificationQueue.builder()
                .targetUserId(event.getTargetUserId())
                .title(event.getTitle())
                .message(event.getMessage())
                .linkTo(event.getLinkTo())
                .status(NotificationQueue.QueueStatus.PENDING)
                .retryCount(0)
                .build();

        queueRepository.save(queueItem);
        log.info("Enqueued notification event for user: {}", event.getTargetUserId());
    }

    /**
     * Xử lý các events trong queue
     * Method này sẽ được gọi bởi scheduled task
     */
    @Override
    @Transactional
    public void processQueue() {
        try {
            // Lấy các events đang pending từ database, giới hạn theo batch size
            List<NotificationQueue> pendingEvents = queueRepository.findPendingEventsForProcessing(
                    NotificationQueue.QueueStatus.PENDING,
                    PageRequest.of(0, batchSize)
            );

            if (pendingEvents.isEmpty()) {
                return;
            }

            log.info("Processing {} notification events from queue", pendingEvents.size());

            for (NotificationQueue queueItem : pendingEvents) {
                try {
                    // Đánh dấu đang xử lý
                    queueItem.setStatus(NotificationQueue.QueueStatus.PROCESSING);
                    queueRepository.save(queueItem);

                    // Chuyển đổi sang NotificationRequestEvent
                    NotificationRequestEvent event = new NotificationRequestEvent();
                    event.setTargetUserId(queueItem.getTargetUserId());
                    event.setTitle(queueItem.getTitle());
                    event.setMessage(queueItem.getMessage());
                    event.setLinkTo(queueItem.getLinkTo());

                    // Xử lý notification
                    notificationService.processNewNotification(event);

                    // Đánh dấu đã xử lý xong
                    queueItem.setStatus(NotificationQueue.QueueStatus.COMPLETED);
                    queueItem.setProcessedAt(Instant.now());
                    queueItem.setErrorMessage(null);
                    queueRepository.save(queueItem);

                    log.info("Successfully processed notification event: {}", queueItem.getQueueId());

                } catch (Exception e) {
                    log.error("Error processing notification event {}: {}", queueItem.getQueueId(), e.getMessage(), e);

                    // Đánh dấu failed và tăng retry count
                    queueItem.setStatus(NotificationQueue.QueueStatus.FAILED);
                    queueItem.setRetryCount(queueItem.getRetryCount() + 1);
                    queueItem.setErrorMessage(e.getMessage());

                    // Nếu retry count < 3, đặt lại về PENDING để retry lần sau
                    if (queueItem.getRetryCount() < 3) {
                        queueItem.setStatus(NotificationQueue.QueueStatus.PENDING);
                    }

                    queueRepository.save(queueItem);
                }
            }
        } catch (Exception e) {
            log.error("Error processing queue: {}", e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả queue items (dùng để test/debug)
     */
    @Override
    @Transactional(readOnly = true)
    public List<QueueStatusResponse> getAllQueueItems() {
        return queueRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy queue items theo status (dùng để test/debug)
     */
    @Override
    @Transactional(readOnly = true)
    public List<QueueStatusResponse> getQueueItemsByStatus(String status) {
        try {
            NotificationQueue.QueueStatus queueStatus = NotificationQueue.QueueStatus.valueOf(status.toUpperCase());
            return queueRepository.findAll().stream()
                    .filter(item -> item.getStatus() == queueStatus)
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Invalid status: {}", status);
            return List.of();
        }
    }

    private QueueStatusResponse mapToResponse(NotificationQueue queue) {
        return QueueStatusResponse.builder()
                .queueId(queue.getQueueId())
                .targetUserId(queue.getTargetUserId())
                .title(queue.getTitle())
                .message(queue.getMessage())
                .linkTo(queue.getLinkTo())
                .status(queue.getStatus())
                .retryCount(queue.getRetryCount())
                .errorMessage(queue.getErrorMessage())
                .createdAt(queue.getCreatedAt())
                .processedAt(queue.getProcessedAt())
                .build();
    }
}
