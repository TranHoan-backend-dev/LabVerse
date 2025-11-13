package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Note entity - Stores user's notes on PDF pages
 * JPA Entity for SQL Server
 */
@Entity
@Table(name = "notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    @Id
    @Column(name = "id", length = 36)
    private String id = UUID.randomUUID().toString();
    
    @Column(name = "paper_id", length = 36, nullable = false)
    private String paperId;
    
    @Column(name = "collection_id", length = 36, nullable = false)
    private String collectionId;
    
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;
    
    @Column(name = "content", length = 255, nullable = false)
    private String content;
    
    @Column(name = "coordination_x", nullable = false)
    private Integer coordinationX;
    
    @Column(name = "coordination_y", nullable = false)
    private Integer coordinationY;
    
    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

