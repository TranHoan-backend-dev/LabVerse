package com.se1853_jv.labverse.domain.infrastructure.ref;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Note;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(
        tableName = "workflow_highlight",
        primaryKeys = {"highlightId", "workflowOwnerId", "workflowPaperId", "workflowCitationId"},
        foreignKeys = {
                @ForeignKey(
                        entity = Note.class,
                        parentColumns = "id",
                        childColumns = "noteId"
                ),
                @ForeignKey(
                        entity = ReadingWorkflow.class,
                        parentColumns = {"userId", "paperId", "collectionId"},
                        childColumns = {"workflowOwnerId", "workflowPaperId", "workflowCitationId"}
                ),
                @ForeignKey(
                        entity = ReadingWorkflow.class,
                        parentColumns = "id",
                        childColumns = "workflowPaperId"
                ),
                @ForeignKey(
                        entity = ReadingWorkflow.class,
                        parentColumns = "id",
                        childColumns = "workflowCitationId"
                )
        }
)
public class WorkflowHighlightCrossRef {
    @NonNull
    String highlightId;

    @NonNull
    String workflowOwnerId;

    @NonNull
    String workflowPaperId;

    @NonNull
    String workflowCitationId;
}
