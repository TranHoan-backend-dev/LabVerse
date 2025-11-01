package com.se1853_jv.reading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reading_workflow_highlight")
public class ReadingWorkflowHighlight {

    @EmbeddedId
    private ReadingWorkflowHighlightId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "collection_id", referencedColumnName = "collection_id", insertable = false, updatable = false),
            @JoinColumn(name = "paper_id", referencedColumnName = "paper_id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    @JsonIgnore
    private ReadingWorkflow readingWorkflow;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Highlight highlight;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

