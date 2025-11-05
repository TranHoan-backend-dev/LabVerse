package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionUserRequest {
    private String collectionId;
    private String memberId;
    private Boolean isAuthor;
}

