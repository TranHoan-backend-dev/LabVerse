package com.se1853_jv.dto.request;

import lombok.*;
import jakarta.validation.constraints.NotBlank;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionRequest {
    @NotBlank
    private String name;
    private String userId; // User ID who creates the collection (PI only)
}
