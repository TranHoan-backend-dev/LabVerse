package com.se1853_jv.labverse.domain.infrastructure.annotation.model;

import androidx.room.Entity;
import androidx.annotation.NonNull;
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
public class Note extends Annotation {
    @PrimaryKey
    @NonNull
    String id;

    @NonNull
    String content;
}
