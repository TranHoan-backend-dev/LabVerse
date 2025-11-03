package com.se1853_jv.labverse.domain.infrastructure.paper.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.se1853_jv.labverse.presentation.paper.utils.AuthorListToStringDeserializer;
import com.se1853_jv.labverse.presentation.paper.utils.SingleValueArrayToStringDeserializer;
import com.se1853_jv.labverse.presentation.paper.utils.YearFromDatePartsDeserializer;

import java.util.List;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaperResearch {
    @PrimaryKey
    @NonNull
    String id;

    @NonNull
    @JsonAlias({"URL"})
    String dataUrl;

    @NonNull
    String description;

    List<String> keyword;

    @NonNull
    @JsonAlias({"message.title"})
    @JsonDeserialize(using = SingleValueArrayToStringDeserializer.class)
    private String title;

    @NonNull
    @JsonAlias({"author"})
    @JsonDeserialize(using = AuthorListToStringDeserializer.class)
    String authors;

    @NonNull
    @JsonAlias({"container-title"})
    @JsonDeserialize(using = SingleValueArrayToStringDeserializer.class)
    String journal;

    @NonNull
    @JsonAlias({"issued"})
    @JsonDeserialize(using = YearFromDatePartsDeserializer.class)
    Integer publicationYear;

    @NonNull
    @JsonAlias({"DOI"})
    String doi;
}
