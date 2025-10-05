package com.se1853_jv.labverse.domain.infrastructure.annotation.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Highlight extends Annotation {
    @PrimaryKey
    @NonNull
    String id;

    @ColumnInfo(name = "color")
    @NonNull
    String colorCode;
}
