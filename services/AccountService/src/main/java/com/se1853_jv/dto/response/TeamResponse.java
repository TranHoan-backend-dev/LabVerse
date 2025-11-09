package com.se1853_jv.dto.response;

import com.se1853_jv.model.Team;
import java.time.LocalDate;

public class TeamResponse {
    private String id;
    private String name;
    private String description;
    private String researchField;
    private Team.PrivacyType privacy;
    private String iconUrl;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private String createdById;
    private String createdByName;
    private String createdByEmail;
    private Integer memberCount;
    private Boolean isMember;
    private TeamMemberResponse currentUserRole;

    public TeamResponse() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResearchField() {
        return researchField;
    }

    public void setResearchField(String researchField) {
        this.researchField = researchField;
    }

    public Team.PrivacyType getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Team.PrivacyType privacy) {
        this.privacy = privacy;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
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

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Boolean getIsMember() {
        return isMember;
    }

    public void setIsMember(Boolean isMember) {
        this.isMember = isMember;
    }

    public TeamMemberResponse getCurrentUserRole() {
        return currentUserRole;
    }

    public void setCurrentUserRole(TeamMemberResponse currentUserRole) {
        this.currentUserRole = currentUserRole;
    }
}

