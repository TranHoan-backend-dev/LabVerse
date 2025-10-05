package com.se1853_jv.labverse.domain.infrastructure.user.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.institution.model.Institution;
import com.se1853_jv.labverse.domain.infrastructure.ref.UserInstitutionCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

public class UserWithInstitution {
    @Embedded
    private Users users;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = UserInstitutionCrossRef.class,
                    parentColumn = "userId",
                    entityColumn = "institutionId"
            )
    )
    private List<Institution> institutions;
}
