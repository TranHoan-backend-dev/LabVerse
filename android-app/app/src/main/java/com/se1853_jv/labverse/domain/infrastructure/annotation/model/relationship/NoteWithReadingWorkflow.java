package com.se1853_jv.labverse.domain.infrastructure.annotation.model.relationship;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.ref.ReadingWorkflowNoteCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

import java.util.List;

public class NoteWithReadingWorkflow {
    @Embedded
    public Note note;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = ReadingWorkflowNoteCrossRef.class,
                    parentColumn = "noteId",
                    entityColumn = "readingWorkflowId"
            )
    )
    public List<ReadingWorkflow> readingWorkflows;
}
