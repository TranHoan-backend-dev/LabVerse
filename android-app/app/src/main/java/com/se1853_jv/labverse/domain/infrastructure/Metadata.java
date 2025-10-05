package com.se1853_jv.labverse.domain.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Metadata {
    private String title;
    private String authors;
    private String journal;
    private int publicationYear;
    private String doi;
}
