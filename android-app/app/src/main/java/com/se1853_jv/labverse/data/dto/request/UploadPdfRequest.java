package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadPdfRequest implements Serializable {
    private String dataUrl; // URL của PDF file (được upload lên storage bởi backend)
    private String description;
    private List<String> keywords;
    private String title;
    private String authors;
    private String journal;
    private Integer publicationYear;
    private String doi; // Optional - backend will auto-generate if null or empty
    private List<String> tags;
}

