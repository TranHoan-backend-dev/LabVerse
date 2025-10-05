package com.se1853_jv.labverse.domain.infrastructure.ref;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.se1853_jv.labverse.domain.enumerate.ReadingTag;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;
import com.se1853_jv.labverse.domain.infrastructure.readinglist.model.ReadingList;

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
        tableName = "paper_readinglist",
        primaryKeys = {"paperId", "readingListId"},
        foreignKeys = {
                @ForeignKey(
                        entity = ReadingList.class,
                        parentColumns = "id",
                        childColumns = "readingListId"
                ),
                @ForeignKey(
                        entity = PaperResearch.class,
                        parentColumns = "id",
                        childColumns = "paperId"
                )
        }
)
public class ReadingListPaperCrossRef {
    @NonNull
    String paperId;

    @NonNull
    String readingListId;

    @NonNull
    LocalDateTime addingDate;

    @NonNull
    ReadingTag status;
}
