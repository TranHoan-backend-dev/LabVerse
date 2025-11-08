package com.se1853_jv.dto.request;

import com.se1853_jv.model.enumerate.AccessLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateMemberAccessRequest {
    @NotBlank
    private String userId; // User ID making the request (must be AUTHOR)

    @NotNull
    private AccessLevel accessLevel; // New access level for the member
}

