package com.se1853_jv.readingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reading_workflow")
public class ReadingWorkflow {

    @EmbeddedId
    private ReadingWorkflowId id;

    @Size(max = 10)
    @Column(name = "status", length = 10)
    private String status; // "unread" | "reading" | "finished"

    @Column(name = "last_page", columnDefinition = "INTEGER")
    private Integer lastPage;

    @Min(0)
    @Max(100)
    @Column(name = "progress", columnDefinition = "INTEGER")
    private Integer progress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = "unread";
        }
        if (this.lastPage == null) {
            this.lastPage = 0;
        }
        if (this.progress == null) {
            this.progress = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        // Auto-update status if progress reaches 100
        if (this.progress != null && this.progress >= 100 && !"finished".equals(this.status)) {
            this.status = "finished";
        }
    }

    // Many-to-Many relationships
    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "reading_workflow_note",
            joinColumns = {
                    @JoinColumn(name = "reading_workflow_collection_id", referencedColumnName = "collection_id"),
                    @JoinColumn(name = "reading_workflow_paper_id", referencedColumnName = "paper_id"),
                    @JoinColumn(name = "reading_workflow_usersid", referencedColumnName = "usersid")
            },
            inverseJoinColumns = @JoinColumn(name = "note_id", referencedColumnName = "id")
    )
    private List<Note> notes = new ArrayList<>();

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "reading_workflow_highlight",
            joinColumns = {
                    @JoinColumn(name = "reading_workflow_collection_id", referencedColumnName = "collection_id"),
                    @JoinColumn(name = "reading_workflow_paper_id", referencedColumnName = "paper_id"),
                    @JoinColumn(name = "reading_workflow_usersid", referencedColumnName = "usersid")
            },
            inverseJoinColumns = @JoinColumn(name = "highlightid", referencedColumnName = "id")
    )
    private List<Highlight> highlights = new ArrayList<>();
}

