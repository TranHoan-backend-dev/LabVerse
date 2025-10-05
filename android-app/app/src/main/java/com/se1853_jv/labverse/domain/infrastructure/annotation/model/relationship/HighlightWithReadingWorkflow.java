package com.se1853_jv.labverse.domain.infrastructure.annotation.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.ref.ReadingWorkflowHighlightCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

import java.util.List;

public class HighlightWithReadingWorkflow {
    @Embedded
    public Highlight highlight;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = ReadingWorkflowHighlightCrossRef.class,
                    parentColumn = "highlightId",
                    entityColumn = "readingWorkflowId"
            )
    )
    public List<ReadingWorkflow> readingWorkflows;
}
