package com.se1853_jv.readingservice.model;

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
@Table(name = "reading_workflow_note")
public class ReadingWorkflowNote {

    @EmbeddedId
    private ReadingWorkflowNoteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "collection_id", referencedColumnName = "collection_id", insertable = false, updatable = false),
            @JoinColumn(name = "paper_id", referencedColumnName = "paper_id", insertable = false, updatable = false),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    })
    @JsonIgnore
    private ReadingWorkflow readingWorkflow;

    // Helper methods to access composite key fields
    public String getCollectionId() {
        return id != null ? id.getCollectionId() : null;
    }

    public String getPaperId() {
        return id != null ? id.getPaperId() : null;
    }

    public String getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public String getNoteId() {
        return id != null ? id.getNoteId() : null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Note note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

