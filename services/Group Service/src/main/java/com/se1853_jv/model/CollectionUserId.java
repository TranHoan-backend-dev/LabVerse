package com.se1853_jv.model;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CollectionUserId implements Serializable {
    @Column(name = "member_id", length = 36)
    private String memberId;

    @Column(name = "Collectionid", length = 36)
    private String collectionId;
}
