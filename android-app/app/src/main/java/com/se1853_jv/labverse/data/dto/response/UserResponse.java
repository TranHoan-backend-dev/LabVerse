package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String role;
    private String createdDate;
    private String updatedDate;
}

