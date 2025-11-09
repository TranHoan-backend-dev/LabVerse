package com.se1853_jv.dto.request;

import com.se1853_jv.model.TeamMember;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddTeamMemberRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Role is required")
    private TeamMember.TeamRole role;

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TeamMember.TeamRole getRole() {
        return role;
    }

    public void setRole(TeamMember.TeamRole role) {
        this.role = role;
    }
}

