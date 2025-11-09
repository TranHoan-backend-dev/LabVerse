package com.se1853_jv.dto.request;

import com.se1853_jv.model.TeamMember;
import jakarta.validation.constraints.NotNull;

public class UpdateMemberRoleRequest {

    @NotNull(message = "Role is required")
    private TeamMember.TeamRole role;

    // Getters and Setters
    public TeamMember.TeamRole getRole() {
        return role;
    }

    public void setRole(TeamMember.TeamRole role) {
        this.role = role;
    }
}

