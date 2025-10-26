package com.se1853_jv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "collection_paper")
public class CollectionPaper {
    @EmbeddedId
    private CollectionPaperId id;

    @MapsId("collectionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false, columnDefinition = "varchar(36)")
    private Collection collection;

    @Size(max = 255)
    @Column(name = "priority")
    private String priority;

    @Size(max = 255)
    @Column(name = "status")
    private String status;

    @Column(name = "adding_date")
    private LocalDate addingDate;

}