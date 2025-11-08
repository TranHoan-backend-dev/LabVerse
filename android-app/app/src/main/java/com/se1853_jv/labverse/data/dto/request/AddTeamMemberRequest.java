package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTeamMemberRequest implements Serializable {
    private String userId;
    private String role; // PI, RESEARCHER, STUDENT
}

