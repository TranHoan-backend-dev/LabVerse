package com.se1853_jv.labverse.domain.infrastructure.ref;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.se1853_jv.labverse.domain.infrastructure.citation.model.Citation;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.enumerate.Priority;
import com.se1853_jv.labverse.domain.enumerate.ReadingTag;

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
        tableName = "paper_citation",
        primaryKeys = {"citationId", "paperResearchId"},
        foreignKeys = {
                @ForeignKey(
                        entity = PaperResearch.class,
                        parentColumns = "id",
                        childColumns = "paperResearchId"
                ),
                @ForeignKey(
                        entity = Citation.class,
                        parentColumns = "id",
                        childColumns = "citationId"
                )
        }
)
public class PaperCitationCrossRef {
    @NonNull
    String citationId;

    @NonNull
    String paperResearchId;
}
