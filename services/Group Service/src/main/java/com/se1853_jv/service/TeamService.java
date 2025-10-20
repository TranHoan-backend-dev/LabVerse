package com.se1853_jv.service;


import com.se1853_jv.dto.TeamDto;
import com.se1853_jv.dto.TeamMemberDto;

import java.util.List;

public interface TeamService {
    TeamDto createTeam(TeamDto dto);
    TeamDto getTeamById(String id);
    List<TeamDto> getAllTeams();
    TeamMemberDto addMember(String teamId, TeamMemberDto memberDto);
    void removeMember(String teamId, String memberId);
    void deleteTeam(String id);
}
