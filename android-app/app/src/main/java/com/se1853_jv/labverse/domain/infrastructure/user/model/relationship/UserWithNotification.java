package com.se1853_jv.labverse.domain.infrastructure.user.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.notification.model.Notification;
import com.se1853_jv.labverse.domain.infrastructure.ref.UsersNotificationCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

public class UserWithNotification {
    @Embedded
    public Users user;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = UsersNotificationCrossRef.class,
                    parentColumn = "userId",
                    entityColumn = "notificationId"
            )
    )
    public List<Notification> notifications;
}
