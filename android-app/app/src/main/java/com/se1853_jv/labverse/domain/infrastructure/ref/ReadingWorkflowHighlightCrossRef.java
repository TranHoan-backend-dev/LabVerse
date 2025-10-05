package com.se1853_jv.labverse.domain.infrastructure.ref;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;
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
        tableName = "readingWorkflow_highlight",
        primaryKeys = {"rwCollectionId", "rwPaperId", "rwUserId", "highlightId"},

        foreignKeys = {
                @ForeignKey(
                        entity = ReadingWorkflow.class,
                        parentColumns = {"rwCollectionId", "rwPaperId", "rwUserId"},
                        childColumns = "readingWorkflowId"
                ),
                @ForeignKey(
                        entity = Highlight.class,
                        parentColumns = "id",
                        childColumns = "highlightId"
                )
        }
)
public class ReadingWorkflowHighlightCrossRef {
    @NonNull
    String rwCollectionId;

    @NonNull
    String rwPaperId;

    @NonNull
    String rwUserId;

    @NonNull
    String highlightId;
}
