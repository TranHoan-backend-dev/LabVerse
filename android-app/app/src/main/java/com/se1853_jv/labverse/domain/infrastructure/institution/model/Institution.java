package com.se1853_jv.labverse.domain.infrastructure.institution.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Institution {
    @PrimaryKey
    @NonNull
    String id;

    @NonNull
    String name;

    @NonNull
    String abbreviation;

    @NonNull
    String type;

    @NonNull
    String country;

    @NonNull
    String address;

    @NonNull
    String websiteUrl;

    @NonNull
    Long createdAt;

    @NonNull
    Long updatedAt;
}
