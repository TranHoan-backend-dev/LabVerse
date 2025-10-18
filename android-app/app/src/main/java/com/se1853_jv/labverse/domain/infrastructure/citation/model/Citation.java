package com.se1853_jv.labverse.domain.infrastructure.citation.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Citation {
    @PrimaryKey
    @NonNull
    String id;

    @NonNull
    String title;

    @NonNull
    String authors;

    @NonNull
    String journal;

    @NonNull
    Integer publicationYear;

    @NonNull
    String doi;
}
