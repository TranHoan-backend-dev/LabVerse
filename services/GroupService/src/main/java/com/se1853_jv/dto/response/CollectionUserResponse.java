package com.se1853_jv.dto.response;

import com.se1853_jv.model.CollectionUser;
import com.se1853_jv.util.IdEncoder;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionUserResponse {
    private String collectionId;
    private String memberId;
    private Boolean isAuthor;

    public static CollectionUserResponse fromEntity(CollectionUser entity) {
        return CollectionUserResponse.builder()
                .collectionId(IdEncoder.encode(entity.getId().getCollectionId()))
                .memberId(IdEncoder.encode(entity.getId().getMemberId()))
                .isAuthor(entity.getIsAuthor())
                .build();
    }
}


