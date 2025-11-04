package com.se1853_jv.dto.response;

import com.se1853_jv.model.Collection;
import com.se1853_jv.util.IdEncoder;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionResponse {
    private String id;
    private String name;

    public static CollectionResponse fromEntity(Collection entity) {
        return CollectionResponse.builder()
                .id(IdEncoder.encode(entity.getId()))
                .name(entity.getName())
                .build();
    }
}
