package com.se1853_jv.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamMemberDto {
    private String id; // join id
    private String userId;
    private String role;
}
