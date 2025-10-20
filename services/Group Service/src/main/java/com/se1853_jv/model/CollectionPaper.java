package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Collection_Paper")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPaper {
    @Id
    private String id;

    @Column(name = "paper_id", nullable = false)
    private String paperId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    private String priority;
    private String status;

    @Column(name = "adding_date")
    private java.time.LocalDateTime addingDate;
}