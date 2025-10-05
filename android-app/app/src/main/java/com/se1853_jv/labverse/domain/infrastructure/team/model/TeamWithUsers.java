package com.se1853_jv.labverse.domain.infrastructure.team.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.ref.UsersTeamCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

public class TeamWithUsers {
    @Embedded
    public Team team;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = UsersTeamCrossRef.class,
                    parentColumn = "teamId",
                    entityColumn = "memberId"
            )
    )
    public List<Users> users;
}
