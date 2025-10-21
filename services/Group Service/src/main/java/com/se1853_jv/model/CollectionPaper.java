package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "Collection_Paper")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPaper {

    @EmbeddedId
    private CollectionPaperId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("collectionId")
    @JoinColumn(name = "Collectionid", nullable = false)
    private Collection collection;

    // chỉ lưu id của paper (thuộc PaperService)
    @Column(name = "Paperid", insertable = false, updatable = false)
    private String paperId;

    @Column(length = 255)
    private String priority;

    @Column(length = 255)
    private String status;

    @Column(name = "adding_date")
    private LocalDate addingDate;
}
