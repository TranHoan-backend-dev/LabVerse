package com.se1853_jv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionPaperId implements java.io.Serializable {
    private static final long serialVersionUID = 39658897897476899L;
    @Size(max = 36)
    @NotNull
    @Column(name = "paper_id", nullable = false, length = 36, columnDefinition = "varchar(36)")
    private String paperId;

    @Size(max = 36)
    @NotNull
    @Column(name = "collection_id", nullable = false, length = 36, columnDefinition = "varchar(36)")
    private String collectionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CollectionPaperId entity = (CollectionPaperId) o;
        return Objects.equals(this.collectionId, entity.collectionId) &&
                Objects.equals(this.paperId, entity.paperId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId, paperId);
    }

}