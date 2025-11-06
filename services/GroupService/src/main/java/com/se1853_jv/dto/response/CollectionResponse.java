package com.se1853_jv.dto.response;

import com.se1853_jv.model.Collection;
import com.se1853_jv.util.IdEncoder;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionResponse {
    private String id;
    private String name;
    private Long paperCount;
    private Long memberCount;
    private String creatorName;
    private String creatorAvatarUrl;
    private Boolean isCreator;  // Whether current user is the creator of this collection

    public static CollectionResponse fromEntity(Collection entity) {
        return CollectionResponse.builder()
                .id(IdEncoder.encode(entity.getId()))
                .name(entity.getName())
                .paperCount(0L)
                .memberCount(0L)
                .build();
    }

    public static CollectionResponse fromEntity(Collection entity, Long paperCount, Long memberCount) {
        return CollectionResponse.builder()
                .id(IdEncoder.encode(entity.getId()))
                .name(entity.getName())
                .paperCount(paperCount != null ? paperCount : 0L)
                .memberCount(memberCount != null ? memberCount : 0L)
                .build();
    }
}
