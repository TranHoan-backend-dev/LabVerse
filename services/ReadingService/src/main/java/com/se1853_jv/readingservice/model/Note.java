package com.se1853_jv.readingservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "note")
public class Note {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id; // varchar(36) - UUID as String

    @NotBlank
    @Size(max = 255)
    @Column(name = "content", length = 255, nullable = false)
    private String content;

    @NotNull
    @Column(name = "coordination_x", columnDefinition = "INTEGER")
    private Integer coordinationX;

    @NotNull
    @Column(name = "coordination_y", columnDefinition = "INTEGER")
    private Integer coordinationY;

    @NotNull
    @Column(name = "page_number", columnDefinition = "INTEGER")
    private Integer pageNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

