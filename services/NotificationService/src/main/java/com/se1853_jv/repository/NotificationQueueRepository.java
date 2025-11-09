package com.se1853_jv.repository;

import com.se1853_jv.model.NotificationQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, UUID> {

    /**
     * Lấy các events đang pending để xử lý
     * Dùng pessimistic lock để tránh xử lý trùng khi có nhiều instance
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT nq FROM NotificationQueue nq WHERE nq.status = :status AND nq.retryCount < 3 ORDER BY nq.createdAt ASC")
    List<NotificationQueue> findPendingEventsForProcessing(@Param("status") NotificationQueue.QueueStatus status, Pageable pageable);

    /**
     * Đánh dấu event đang được xử lý
     */
    @Modifying
    @Query("UPDATE NotificationQueue nq SET nq.status = :newStatus WHERE nq.queueId = :id")
    void updateStatus(@Param("id") UUID id, @Param("newStatus") NotificationQueue.QueueStatus newStatus);

    /**
     * Đánh dấu event đã xử lý xong
     */
    @Modifying
    @Query("UPDATE NotificationQueue nq SET nq.status = :newStatus, nq.processedAt = :processedAt WHERE nq.queueId = :id")
    void markAsCompleted(@Param("id") UUID id, @Param("newStatus") NotificationQueue.QueueStatus newStatus, @Param("processedAt") Instant processedAt);

    /**
     * Đánh dấu event failed và tăng retry count
     */
    @Modifying
    @Query("UPDATE NotificationQueue nq SET nq.status = :newStatus, nq.retryCount = nq.retryCount + 1, nq.errorMessage = :errorMessage WHERE nq.queueId = :id")
    void markAsFailed(@Param("id") UUID id, @Param("newStatus") NotificationQueue.QueueStatus newStatus, @Param("errorMessage") String errorMessage);
}
