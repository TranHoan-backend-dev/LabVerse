package com.se1853_jv.labverse.domain.infrastructure.workflow.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.ref.ReadingWorkflowHighlightCrossRef;

import java.util.List;

public class ReadingWorkflowWithHighlight {
    @Embedded
    ReadingWorkflow readingWorkflow;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = ReadingWorkflowHighlightCrossRef.class,
                    parentColumn = "readingWorkflowId",
                    entityColumn = "highlightId"
            )
    )
    List<Highlight> highlights;
}
