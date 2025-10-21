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
public class CollectionPaperId implements Serializable {
    @Column(name = "Paperid", length = 36)
    private String paperId;

    @Column(name = "Collectionid", length = 36)
    private String collectionId;
}
