package com.se1853_jv.labverse.domain.infrastructure.collection.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.ref.CollectionPaperCrossRef;

import java.util.List;

public class CollectionWithPaper {
    @Embedded
    public Collections collections;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = CollectionPaperCrossRef.class,
                    parentColumn = "collectionId",
                    entityColumn = "paperResearchId"
            )
    )
    public List<PaperResearch> papers;

}
