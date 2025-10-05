package com.se1853_jv.labverse.domain.infrastructure.tag.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.ref.TagPaperCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;

import java.util.List;

public class TagWithPaper {
    @Embedded
    public Tag tag;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = TagPaperCrossRef.class,
                    parentColumn = "tagId",
                    entityColumn = "paperId"
            )
    )
    public List<PaperResearch> papers;
}
