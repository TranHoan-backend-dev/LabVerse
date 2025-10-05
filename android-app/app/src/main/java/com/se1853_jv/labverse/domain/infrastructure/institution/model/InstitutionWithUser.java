package com.se1853_jv.labverse.domain.infrastructure.institution.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.ref.UserInstitutionCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

public class InstitutionWithUser {
    @Embedded
    private Institution institution;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = UserInstitutionCrossRef.class,
                    parentColumn = "institutionId",
                    entityColumn = "userId"
            )
    )
    private List<Users> users;
}
