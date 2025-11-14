package com.se1853_jv.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ChangeUserRoleRequest {
    
    @NotBlank(message = "Role ID is required")
    private String roleId;

    public ChangeUserRoleRequest() {
    }

    public ChangeUserRoleRequest(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }
}

