package com.se1853_jv.labverse.data.dto.response;

import com.se1853_jv.labverse.domain.enumerate.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionUserResponse {
    private String collectionId;
    private String memberId;
    private Boolean isAuthor; // Deprecated, kept for backward compatibility
    private AccessLevel accessLevel;
}

