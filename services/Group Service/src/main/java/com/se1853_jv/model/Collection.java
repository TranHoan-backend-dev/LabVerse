package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Collection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionUser> members = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionPaper> papers = new ArrayList<>();
}
