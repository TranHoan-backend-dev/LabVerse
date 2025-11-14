package com.se1853_jv.service;

import com.se1853_jv.dto.response.AdminUserDetailsResponse;
import com.se1853_jv.dto.response.OverviewStatisticsResponse;
import com.se1853_jv.dto.response.TeamResponse;
import com.se1853_jv.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public interface AdminService {
    // User Management
    Page<UserResponse> getAllUsers(int page, int size, String search, String role, Boolean isActive);
    AdminUserDetailsResponse getUserDetails(String userId);
    void activateUser(String userId);
    void deactivateUser(String userId);
    UserResponse changeUserRole(String userId, String roleId);
    void deleteUser(String userId);
    
    // Team Management
    Page<TeamResponse> getAllTeams(int page, int size, String search, String privacy);
    void deleteTeam(String teamId);
    
    // Statistics
    OverviewStatisticsResponse getOverviewStatistics();
}

