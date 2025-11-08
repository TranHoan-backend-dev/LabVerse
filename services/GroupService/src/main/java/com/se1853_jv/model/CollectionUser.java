package com.se1853_jv.model;

import com.se1853_jv.model.enumerate.AccessLevel;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", length = 20)
    private AccessLevel accessLevel;

    /**
     * Get access level, defaulting based on isAuthor if not set
     * This provides backward compatibility with existing data
     */
    public AccessLevel getAccessLevel() {
        if (accessLevel != null) {
            return accessLevel;
        }
        // Backward compatibility: if isAuthor is true, return AUTHOR
        if (Boolean.TRUE.equals(isAuthor)) {
            return AccessLevel.AUTHOR;
        }
        // Default to CONTRIBUTOR for existing members
        return AccessLevel.CONTRIBUTOR;
    }
}