package com.se1853_jv.labverse.domain.infrastructure.user.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;
import com.se1853_jv.labverse.domain.infrastructure.ref.CollectionUsersCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

public class UsersWithCollection {
    @Embedded
    public Users user;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = CollectionUsersCrossRef.class,
                    parentColumn = "memberId",
                    entityColumn = "collectionId"
            )
    )
    public List<Collections> collections;
}
