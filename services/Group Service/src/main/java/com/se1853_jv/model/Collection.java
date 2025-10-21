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
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 255)
    private String name;

    // giữ danh sách paper ids dạng JSON hoặc chuỗi để hỗ trợ sync nhanh
    @Column(name = "paper_ids_list", length = 255)
    private String paperIdsList;

    // Quan hệ 1-n với bảng Collection_Users
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionUser> members = new ArrayList<>();

    // Quan hệ 1-n với bảng Collection_Paper
    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionPaper> papers = new ArrayList<>();
}
