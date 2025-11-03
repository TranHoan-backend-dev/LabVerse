package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPaperDetailResponse {
    private String paperId;
    private String title;
    private String authors;
    private String journal;
    private Integer publicationYear;
    private String priority;
    private String status;
    private String addingDate;
}

