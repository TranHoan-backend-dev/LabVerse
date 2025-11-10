package com.se1853_jv.labverse.domain.infrastructure.team.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
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
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Team {
    @PrimaryKey
    @NonNull
    String id;

    @NonNull
    String name;

    String description;

    @ColumnInfo(name = "research_field")
    String researchField;

    @NonNull
    String privacy; // PUBLIC or PRIVATE

    @ColumnInfo(name = "icon_url")
    String iconUrl;

    @ColumnInfo(name = "created_date")
    String createdDate;

    @ColumnInfo(name = "updated_date")
    String updatedDate;

    @ColumnInfo(name = "created_by_id")
    String createdById;

    @ColumnInfo(name = "created_by_name")
    String createdByName;

    @ColumnInfo(name = "created_by_email")
    String createdByEmail;

    @ColumnInfo(name = "member_count")
    Integer memberCount;

    @ColumnInfo(name = "is_member")
    Boolean isMember;
}
