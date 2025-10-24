package com.se1853_jv.labverse.domain.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BibEntry {
    private String type, title, author, year, source, pages;
}
