package com.se1853_jv.repository;

import com.se1853_jv.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

    // Tìm tất cả tokens của 1 user
    List<UserDevice> findByUserId(UUID userId);

    // Tìm xem token này đã tồn tại chưa
    Optional<UserDevice> findByFcmToken(String fcmToken);
}
