package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String userFullName;
    private String userEmail;
    private String userAvatarUrl;
    private String role; // PI, RESEARCHER, STUDENT
    private String joinedDate;
}

