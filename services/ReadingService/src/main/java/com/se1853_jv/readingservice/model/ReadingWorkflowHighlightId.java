package com.se1853_jv.readingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class ReadingWorkflowHighlightId implements Serializable {

    @Column(name = "collection_id", length = 36, nullable = false)
    private String collectionId;

    @Column(name = "paper_id", length = 36, nullable = false)
    private String paperId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "highlight_id", length = 36, nullable = false)
    private String highlightId;
}

