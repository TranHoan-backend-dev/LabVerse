package com.se1853_jv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity lưu trữ thông tin mỗi lần export annotation của một paper trong collection.
 * Dữ liệu export được lưu dạng JSON để dễ dàng tái sử dụng khi import.
 */
@Entity
@Table(name = "annotation_exports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationExport {

    @Id
    @Column(name = "id", length = 36)
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Column(name = "paper_id", length = 36, nullable = false)
    private String paperId;

    @Column(name = "collection_id", length = 36, nullable = false)
    private String collectionId;

    @Column(name = "exported_by", length = 36, nullable = false)
    private String exportedBy;

    @Column(name = "exported_at", nullable = false)
    @Builder.Default
    private LocalDateTime exportedAt = LocalDateTime.now();

    /**
     * Payload JSON chứa danh sách note và highlight tại thời điểm export.
     */
    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "total_notes", nullable = false)
    private Integer totalNotes;

    @Column(name = "total_highlights", nullable = false)
    private Integer totalHighlights;
}

