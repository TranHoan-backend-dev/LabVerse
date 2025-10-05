package com.se1853_jv.labverse.domain.infrastructure.paper.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.ref.CollectionPaperCrossRef;

import java.util.List;

public class PaperWithCollection {
    @Embedded
    public PaperResearch paper;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = CollectionPaperCrossRef.class,
                    parentColumn = "paperResearchId",
                    entityColumn = "collectionId"
            )
    )
    public List<Collections> collections;
}
