package com.se1853_jv.dto.response;

import com.se1853_jv.util.IdEncoder;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionMemberResponse {
    private String memberId; // encoded
    private boolean isAuthor;

    public static CollectionMemberResponse fromEntity(String memberId, boolean isAuthor) {
        return CollectionMemberResponse.builder()
                .memberId(IdEncoder.encode(memberId))
                .isAuthor(isAuthor)
                .build();
    }
}
