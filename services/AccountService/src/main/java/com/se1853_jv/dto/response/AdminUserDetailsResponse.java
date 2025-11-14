package com.se1853_jv.dto.response;

import java.time.LocalDate;

public class AdminUserDetailsResponse {
    private String id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String role;
    private Boolean isActive;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private Long paperCount;
    private Long teamCount;
    private Long collectionCount;

    public AdminUserDetailsResponse() {
    }

    public AdminUserDetailsResponse(String id, String email, String username, String fullName,
                                   String avatarUrl, String role, Boolean isActive,
                                   LocalDate createdDate, LocalDate updatedDate,
                                   Long paperCount, Long teamCount, Long collectionCount) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.isActive = isActive;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.paperCount = paperCount;
        this.teamCount = teamCount;
        this.collectionCount = collectionCount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Long getPaperCount() {
        return paperCount;
    }

    public void setPaperCount(Long paperCount) {
        this.paperCount = paperCount;
    }

    public Long getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(Long teamCount) {
        this.teamCount = teamCount;
    }

    public Long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(Long collectionCount) {
        this.collectionCount = collectionCount;
    }
}

