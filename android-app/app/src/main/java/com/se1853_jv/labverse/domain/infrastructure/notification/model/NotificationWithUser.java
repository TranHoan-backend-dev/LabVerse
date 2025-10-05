package com.se1853_jv.labverse.domain.infrastructure.notification.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.ref.UsersNotificationCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

public class NotificationWithUser {
    @Embedded
    public Notification notification;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = UsersNotificationCrossRef.class,
                    parentColumn = "notificationId",
                    entityColumn = "userId"
            )
    )
    public List<Users> users;
}
