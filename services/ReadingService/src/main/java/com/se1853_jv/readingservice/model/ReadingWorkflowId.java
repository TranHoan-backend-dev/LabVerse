package com.se1853_jv.readingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ReadingWorkflowId implements Serializable {

    @Column(name = "collection_id", length = 255, nullable = false) // Increased to support encoded IDs
    private String collectionId;

    @Column(name = "paper_id", length = 255, nullable = false) // Increased to support encoded IDs
    private String paperId;

    @Column(name = "usersid", length = 255, nullable = false) // Increased to support encoded IDs
    private String usersid; // Note: ERD uses "Usersid" (lowercase in DB)
}

