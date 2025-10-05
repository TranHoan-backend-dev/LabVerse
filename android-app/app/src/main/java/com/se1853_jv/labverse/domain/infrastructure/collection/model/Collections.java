package com.se1853_jv.labverse.domain.infrastructure.collection.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Collections {
    @PrimaryKey
    @NonNull
    String id;

    String name;
}
