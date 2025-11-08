package com.se1853_jv.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateCollectionRequest {
    @NotBlank
    private String name;
    private String userId; // User ID who wants to update (must be the author)
}

