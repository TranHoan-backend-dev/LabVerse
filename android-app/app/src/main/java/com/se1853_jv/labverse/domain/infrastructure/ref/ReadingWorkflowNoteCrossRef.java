package com.se1853_jv.labverse.domain.infrastructure.ref;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.se1853_jv.labverse.domain.infrastructure.annotation.model.Highlight;
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
        tableName = "user_highlight",
        primaryKeys = {"ownerId", "noteId"},

        foreignKeys = {
                @ForeignKey(
                        entity = ReadingWorkflow.class,
                        parentColumns = {"userId", "paperId", "collectionId"},
                        childColumns = {"rwCollectionId", "rwPaperId", "rwUserId"}
                ),
                @ForeignKey(
                        entity = Note.class,
                        parentColumns = "id",
                        childColumns = "noteId"
                )
        }
)
public class ReadingWorkflowNoteCrossRef {
    @NonNull
    String rwCollectionId;

    @NonNull
    String rwPaperId;

    @NonNull
    String rwUserId;

    @NonNull
    String noteId;
}
