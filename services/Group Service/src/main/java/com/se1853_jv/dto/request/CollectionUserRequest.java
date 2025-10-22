package com.se1853_jv.dto.request;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionUserRequest {
    @NotBlank
    private String collectionId;

    @NotBlank
    private String memberId;

    private Boolean isAuthor; // true nếu là PI hoặc người tạo
}


