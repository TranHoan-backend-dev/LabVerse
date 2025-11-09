package com.se1853_jv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for exporting annotations
 * Contains all notes and highlights for a specific paper and collection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportAnnotationsResponse {
    private String paperId;
    private String collectionId;
    private String exportedBy;
    private String exportedAt;
    private List<NoteResponse> notes;
    private List<HighlightResponse> highlights;
}

