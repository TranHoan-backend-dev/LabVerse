package com.se1853_jv.dto.request;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionMemberRequest {
    private String memberId;
    private boolean isAuthor;
}
