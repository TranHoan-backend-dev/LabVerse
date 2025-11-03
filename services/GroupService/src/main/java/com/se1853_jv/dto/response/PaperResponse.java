package com.se1853_jv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaperResponse {
    private String id;
    private String dataUrl;
    private List<String> keywords;
    private String title;
    private String authors;
    private String journal;
    private Integer publicationYear;
    private String doi;
    private String description;
}

