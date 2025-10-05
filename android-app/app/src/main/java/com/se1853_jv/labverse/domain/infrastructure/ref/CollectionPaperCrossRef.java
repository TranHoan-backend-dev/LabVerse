package com.se1853_jv.labverse.domain.infrastructure.ref;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.se1853_jv.labverse.domain.enumerate.Priority;
import com.se1853_jv.labverse.domain.enumerate.ReadingTag;
import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

import java.time.LocalDateTime;

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
        tableName = "collection_paper",
        primaryKeys = {"collectionId", "paperResearchId"},
        foreignKeys = {
                @ForeignKey(
                        entity = PaperResearch.class,
                        parentColumns = "id",
                        childColumns = "paperResearchId"
                ),
                @ForeignKey(
                        entity = Collections.class,
                        parentColumns = "id",
                        childColumns = "collectionId"
                )
        }
)
public class CollectionPaperCrossRef {
    @NonNull
    String collectionId;

    @NonNull
    String paperResearchId;

    @NonNull
    Priority priority;

    @NonNull
    ReadingTag status;

    LocalDateTime addingDate;
}
