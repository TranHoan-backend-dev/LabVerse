package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse implements Serializable {
    private String id;
    private String name;
    private String description;
    private String researchField;
    private String privacy; // PUBLIC or PRIVATE
    private String iconUrl;
    private String createdDate;
    private String updatedDate;
    private String createdById;
    private String createdByName;
    private String createdByEmail;
    private Integer memberCount;
    private Boolean isMember;
    private TeamMemberResponse currentUserRole;
}

