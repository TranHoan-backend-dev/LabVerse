package com.se1853_jv.labverse.data.dto.response;

import com.se1853_jv.labverse.domain.enumerate.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionResponse implements Serializable {
    private String id;
    private String name;
    private Long paperCount;
    private Long memberCount;
    private String creatorName;
    private String creatorAvatarUrl;
    private AccessLevel currentUserAccessLevel; // Access level of the current user in this collection
}


