package com.se1853_jv.labverse.data.dto.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchPapersRequest {
    @SerializedName("query")
    private String query; // General search across title, authors, journal, keywords

    @SerializedName("title")
    private String title;

    @SerializedName("authors")
    private String authors;

    @SerializedName("journal")
    private String journal;

    @SerializedName("keywords")
    private List<String> keywords;

    @SerializedName("doi")
    private String doi;

    @SerializedName("yearFrom")
    private Integer yearFrom;

    @SerializedName("yearTo")
    private Integer yearTo;

    @SerializedName("pageIndex")
     @Default
    private Integer pageIndex = 0;

    @SerializedName("pageSize")
    @Default
    private Integer pageSize = 10;

    @SerializedName("sortBy")
    private String sortBy; // "title", "publicationYear", "authors", "journal"

    @SerializedName("sortOrder")
    @Default
    private String sortOrder = "asc"; // "asc" or "desc"
}

