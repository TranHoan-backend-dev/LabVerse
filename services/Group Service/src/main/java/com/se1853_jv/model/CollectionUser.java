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

    @EmbeddedId
    private CollectionUserId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("collectionId") // map với khóa tổng hợp
    @JoinColumn(name = "Collectionid", nullable = false)
    private Collection collection;

    // chỉ lưu id của user, không join sang UserService
    @Column(name = "member_id", insertable = false, updatable = false)
    private String memberId;

    @Column(name = "isAuthor")
    private Boolean isAuthor; // bạn có thể đổi sang boolean nếu cần
}
