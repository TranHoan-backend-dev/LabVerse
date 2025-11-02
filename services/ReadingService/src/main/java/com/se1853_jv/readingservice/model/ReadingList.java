package com.se1853_jv.readingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "reading_list")
public class ReadingList {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id; // varchar(36) - UUID as String

    @Size(max = 255)
    @Column(name = "name", length = 255, nullable = true)
    private String name; // nullable per ERD

    @Column(name = "user_ids_list", length = 255, nullable = true)
    private String userIdsList; // varchar(255) nullable - JSON array string: ["user1", "user2"]

    @Column(name = "paper_ids_list", length = 255, nullable = true)
    private String paperIdsList; // varchar(255) nullable - JSON array string: ["paper1", "paper2"]

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

