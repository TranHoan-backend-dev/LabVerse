package com.se1853_jv.reading.model;

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
public class ReadingWorkflowNoteId implements Serializable {

    @Column(name = "collection_id", length = 36, nullable = false)
    private String collectionId;

    @Column(name = "paper_id", length = 36, nullable = false)
    private String paperId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "note_id", length = 36, nullable = false)
    private String noteId;
}

