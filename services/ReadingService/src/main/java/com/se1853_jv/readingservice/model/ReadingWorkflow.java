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

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status; // "unread" | "reading" | "finished"

    @Column(name = "last_page")
    private Integer lastPage;

    @Min(0)
    @Max(100)
    @Column(name = "progress")
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
}

