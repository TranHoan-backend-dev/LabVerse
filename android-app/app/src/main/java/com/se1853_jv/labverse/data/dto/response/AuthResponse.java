package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type;
    private String userId;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String role;
    
    // Constructor mặc định sẽ set type = "Bearer"
    public AuthResponse(String token, String userId, String email, String username, 
                        String fullName, String avatarUrl, String role) {
        this.token = token;
        this.type = "Bearer";
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}

