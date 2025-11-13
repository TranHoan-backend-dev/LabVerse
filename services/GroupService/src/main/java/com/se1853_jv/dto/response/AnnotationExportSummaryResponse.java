package com.se1853_jv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO rút gọn dùng để hiển thị danh sách các bản export annotation trong collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationExportSummaryResponse {
    private String exportId;
    private String paperId;
    private String collectionId;
    private String exportedBy;
    private String exportedAt;
    private Integer totalNotes;
    private Integer totalHighlights;
}

