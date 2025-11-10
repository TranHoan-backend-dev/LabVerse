package com.se1853_jv.labverse.domain.infrastructure.workflow.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;
import com.se1853_jv.labverse.domain.enumerate.WorkflowStatus;

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
        primaryKeys = {"userId", "paperId", "collectionId"},
        foreignKeys = {
                @ForeignKey(
                        entity = Users.class,
                        parentColumns = "id",
                        childColumns = "userId"
                ),
                @ForeignKey(
                        entity = PaperResearch.class,
                        parentColumns = "id",
                        childColumns = "paperId"
                ),
                @ForeignKey(
                        entity = Collections.class,
                        parentColumns = "id",
                        childColumns = "collectionId",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class ReadingWorkflow {
    @NonNull
    String collectionId;

    @NonNull
    String paperId;

    @NonNull
    String userId;

    @NonNull
    WorkflowStatus status;

    @NonNull
    Integer progress;

    @NonNull
    Integer lastPage;
}
