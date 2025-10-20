package com.se1853_jv.controller;

import com.se1853_jv.dto.TeamDto;
import com.se1853_jv.dto.TeamMemberDto;
import com.se1853_jv.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/groups/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody TeamDto dto) {
        return ResponseEntity.ok(teamService.createTeam(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeam(@PathVariable String id) {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    @GetMapping
    public ResponseEntity<List<TeamDto>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @PostMapping("/{teamId}/members")
    public ResponseEntity<TeamMemberDto> addMember(@PathVariable String teamId, @Valid @RequestBody TeamMemberDto dto) {
        return ResponseEntity.ok(teamService.addMember(teamId, dto));
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable String teamId, @PathVariable String memberId) {
        teamService.removeMember(teamId, memberId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable String id) {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}