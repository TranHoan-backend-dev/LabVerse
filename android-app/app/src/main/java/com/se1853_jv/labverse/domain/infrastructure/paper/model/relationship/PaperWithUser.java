package com.se1853_jv.labverse.domain.infrastructure.paper.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.ref.FavoriteCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

public class PaperWithUser {
    @Embedded
    public PaperResearch paper;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = FavoriteCrossRef.class,
                    parentColumn = "paperResearchId",
                    entityColumn = "userId"
            )
    )
    public List<Users> users;
}
