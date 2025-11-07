package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollectionRequest {
    private String name;
    private String userId; // User ID who wants to update (must be the author)
}

