package com.se1853_jv.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionMemberDto {
    private String id;
    private String memberId;
    private boolean isAuthor;
}