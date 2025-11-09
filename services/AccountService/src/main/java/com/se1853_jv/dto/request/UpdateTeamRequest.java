package com.se1853_jv.dto.request;

import com.se1853_jv.model.Team;
import jakarta.validation.constraints.Size;

public class UpdateTeamRequest {

    @Size(max = 255, message = "Team name must not exceed 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 255, message = "Research field must not exceed 255 characters")
    private String researchField;

    private Team.PrivacyType privacy;

    private String iconUrl;

    // Getters and Setters
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
}

