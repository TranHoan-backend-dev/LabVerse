package com.se1853_jv.repository;

import com.se1853_jv.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // get all noti của user mới => cũ
    // Dùng index idx_user_id_created_at để query nhanh
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}