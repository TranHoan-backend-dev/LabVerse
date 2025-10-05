package com.se1853_jv.labverse.domain.infrastructure.role.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.se1853_jv.labverse.domain.enumerate.Role;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Entity(tableName = "role")
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Roles {
    @PrimaryKey
    @NonNull
    String id;

    @NonNull
    @ColumnInfo(name = "name")
    Role role;
}
