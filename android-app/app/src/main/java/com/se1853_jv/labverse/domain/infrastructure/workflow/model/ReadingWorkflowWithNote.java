package com.se1853_jv.labverse.domain.infrastructure.workflow.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.ref.ReadingWorkflowNoteCrossRef;

import java.util.List;

public class ReadingWorkflowWithNote {
    @Embedded
    Note note;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = ReadingWorkflowNoteCrossRef.class,
                    parentColumn = "readingWorkflowId",
                    entityColumn = "noteId"
            )
    )
    List<ReadingWorkflow> highlights;
}
