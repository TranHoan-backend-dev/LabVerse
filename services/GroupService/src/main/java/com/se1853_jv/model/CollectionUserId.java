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
public class CollectionUserId implements java.io.Serializable {
    private static final long serialVersionUID = 1232318894950822789L;
    @Size(max = 36)
    @NotNull
    @Column(name = "member_id", nullable = false, length = 36, columnDefinition = "varchar(36)")
    private String memberId;

    @Size(max = 36)
    @NotNull
    @Column(name = "collection_id", nullable = false, length = 36, columnDefinition = "varchar(36)")
    private String collectionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CollectionUserId entity = (CollectionUserId) o;
        return Objects.equals(this.collectionId, entity.collectionId) &&
                Objects.equals(this.memberId, entity.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionId, memberId);
    }

}