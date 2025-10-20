package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Collection_Users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionUser {
    @Id
    private String id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    private Collection collection;

    @Column(name = "is_author")
    private boolean isAuthor;
}
