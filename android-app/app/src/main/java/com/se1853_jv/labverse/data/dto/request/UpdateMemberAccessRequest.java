package com.se1853_jv.labverse.data.dto.request;

import com.se1853_jv.labverse.domain.enumerate.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberAccessRequest {
    private String userId; // User ID making the request (must be AUTHOR)
    private AccessLevel accessLevel; // New access level for the member
}

