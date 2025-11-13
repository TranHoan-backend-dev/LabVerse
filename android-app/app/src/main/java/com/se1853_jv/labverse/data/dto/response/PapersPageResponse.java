package com.se1853_jv.labverse.data.dto.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.se1853_jv.labverse.domain.infrastructure.paper.model.PaperResearch;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PapersPageResponse {
    @SerializedName("papers")
    private List<PaperResearch> content;
    @SerializedName("totalElements")
    private long totalElements;
    private int totalPages;
    private int number; // current page number (0-indexed)
    private int size; // page size
}






