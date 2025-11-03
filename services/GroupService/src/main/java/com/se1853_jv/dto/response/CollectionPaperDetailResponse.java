package com.se1853_jv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPaperDetailResponse {
    private String paperId;
    private String title;
    private String authors;
    private String journal;
    private Integer publicationYear;
    private String priority;
    private String status;
    private LocalDate addingDate;
}

