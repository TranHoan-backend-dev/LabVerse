package com.se1853_jv.labverse.domain.infrastructure.notification.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.notification.model.Notification;

@Dao
public interface NotificationRepository {
    @Insert
    void create(Notification noti);

    @Update
    void update(Notification noti);

    @Delete
    void delete(Notification noti);

    @Query("DELETE FROM Notification WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Notification WHERE id = :id")
    Notification getById(String id);
}
