package com.se1853_jv.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    private String id;
    private String name;
    private List<TeamMemberDto> members;
}