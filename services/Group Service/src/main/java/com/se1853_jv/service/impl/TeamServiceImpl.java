package com.se1853_jv.service.impl;

import com.se1853_jv.dto.TeamDto;
import com.se1853_jv.dto.TeamMemberDto;
import com.se1853_jv.exception.ResourceNotFoundException;
import com.se1853_jv.model.Team;
import com.se1853_jv.model.UserTeam;
import com.se1853_jv.repository.TeamRepository;
import com.se1853_jv.repository.UserTeamRepository;
import com.se1853_jv.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.se1853_jv.util.MapperUtil;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final UserTeamRepository userTeamRepository;

    @Override
    public TeamDto createTeam(TeamDto dto) {
        Team team = Team.builder()
                .id(UUID.randomUUID().toString())
                .name(dto.getName())
                .build();
        team = teamRepository.save(team);
        return MapperUtil.toTeamDto(team);
    }

    @Override
    public TeamDto getTeamById(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
        return MapperUtil.toTeamDto(team);
    }

    @Override
    public List<TeamDto> getAllTeams() {
        return teamRepository.findAll().stream().map(MapperUtil::toTeamDto).collect(Collectors.toList());
    }

    @Override
    public TeamMemberDto addMember(String teamId, TeamMemberDto memberDto) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
        UserTeam userTeam = UserTeam.builder()
                .id(UUID.randomUUID().toString())
                .userId(memberDto.getUserId())
                .role(memberDto.getRole())
                .team(team)
                .build();
        userTeam = userTeamRepository.save(userTeam);
        // add to collection in memory
        team.getMembers().add(userTeam);
        teamRepository.save(team);
        return MapperUtil.toTeamMemberDto(userTeam);
    }

    @Override
    public void removeMember(String teamId, String memberId) {
        UserTeam ut = userTeamRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found: " + memberId));
        userTeamRepository.delete(ut);
    }

    @Override
    public void deleteTeam(String id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
        teamRepository.delete(team);
    }
}