package com.se1853_jv.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "collection_user")
public class CollectionUser {
    @EmbeddedId
    private CollectionUserId id;

    @MapsId("collectionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false, columnDefinition = "varchar(36)")
    private Collection collection;


    @Column(name = "isAuthor")
    private Boolean isAuthor;

}