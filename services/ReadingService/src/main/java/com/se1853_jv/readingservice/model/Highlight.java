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
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "highlight")
public class Highlight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "color", length = 50, nullable = false)
    private String color;

    @NotNull
    @Column(name = "coordination_x")
    private Integer coordinationX;

    @NotNull
    @Column(name = "coordination_y")
    private Integer coordinationY;

    @NotNull
    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

