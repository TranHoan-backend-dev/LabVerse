package com.se1853_jv.labverse.presentation.library.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryPaper {
    private String id;
    private String title;
    private String authors;
    private String journal;
    private Integer year;
    private String status; // "Unread", "Reading", "Finished"
    private int citationCount;
    private int readCount;
    private boolean isFavorite;
    private String addedDate;
    private String lastReadDate;
    private int progress; // 0-100 for reading progress
    private String statusColor; // "blue", "yellow", "green"
}
