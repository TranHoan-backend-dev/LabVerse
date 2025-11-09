package com.se1853_jv.service;

import com.se1853_jv.dto.request.AddTeamMemberRequest;
import com.se1853_jv.dto.request.CreateTeamRequest;
import com.se1853_jv.dto.request.UpdateTeamRequest;
import com.se1853_jv.dto.request.UpdateMemberRoleRequest;
import com.se1853_jv.dto.response.TeamMemberResponse;
import com.se1853_jv.dto.response.TeamResponse;
import com.se1853_jv.exception.BadRequestException;
import com.se1853_jv.exception.ResourceNotFoundException;
import com.se1853_jv.model.Team;
import com.se1853_jv.model.TeamMember;
import com.se1853_jv.model.User;
import com.se1853_jv.repository.TeamMemberRepository;
import com.se1853_jv.repository.TeamRepository;
import com.se1853_jv.repository.UserRepository;
import com.se1853_jv.util.IdEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, 
                       TeamMemberRepository teamMemberRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TeamResponse createTeam(String userId, CreateTeamRequest request) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Team team = new Team();
        team.setName(request.getName().trim());
        team.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        team.setResearchField(request.getResearchField() != null ? request.getResearchField().trim() : null);
        team.setPrivacy(request.getPrivacy() != null ? request.getPrivacy() : Team.PrivacyType.PRIVATE);
        team.setIconUrl(request.getIconUrl());
        team.setCreatedBy(creator);

        Team savedTeam = teamRepository.save(team);

        // Add creator as PI member
        TeamMember creatorMember = new TeamMember(savedTeam, creator, TeamMember.TeamRole.PI);
        teamMemberRepository.save(creatorMember);

        return mapToTeamResponse(savedTeam, userId);
    }

    public TeamResponse getTeamById(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check access: public team or user is creator or member
        if (team.getPrivacy() == Team.PrivacyType.PRIVATE 
            && !team.getCreatedBy().getId().equals(userId)
            && !teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new BadRequestException("You do not have access to this team");
        }

        return mapToTeamResponse(team, userId);
    }

    public Page<TeamResponse> getTeams(String userId, String search, String researchField, 
                                       Team.PrivacyType privacy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Team> teams;

        if (search != null || researchField != null || privacy != null) {
            teams = teamRepository.searchTeams(search, researchField, privacy, userId, pageable);
        } else {
            teams = teamRepository.findAccessibleTeams(userId, pageable);
        }

        return teams.map(team -> mapToTeamResponse(team, userId));
    }

    @Transactional
    public TeamResponse updateTeam(String teamId, String userId, UpdateTeamRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " +teamId));

        // Only creator can update team
        if (!team.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Only team creator can update the team");
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            team.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            team.setDescription(request.getDescription().trim());
        }
        if (request.getResearchField() != null) {
            team.setResearchField(request.getResearchField().trim());
        }
        if (request.getPrivacy() != null) {
            team.setPrivacy(request.getPrivacy());
        }
        if (request.getIconUrl() != null) {
            team.setIconUrl(request.getIconUrl());
        }

        Team updatedTeam = teamRepository.save(team);
        return mapToTeamResponse(updatedTeam, userId);
    }

    @Transactional
    public void deleteTeam(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Only creator can delete team
        if (!team.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Only team creator can delete the team");
        }

        // Delete all team members first
        teamMemberRepository.deleteAll(teamMemberRepository.findByTeamId(teamId));
        
        // Delete team
        teamRepository.delete(team);
    }

    @Transactional
    public TeamMemberResponse addMember(String teamId, String userId, AddTeamMemberRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if user to add exists
        User userToAdd = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        // Check if user is already a member
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, request.getUserId())) {
            throw new BadRequestException("User is already a member of this team");
        }

        // Check permissions:
        // 1. If user is adding themselves (self-join), allow only if team is PUBLIC
        // 2. If user is adding someone else, require creator or PI permission
        boolean isSelfJoin = userId.equals(request.getUserId());
        
        // Log for debugging
        System.out.println("addMember - userId (requester): " + userId);
        System.out.println("addMember - request.getUserId(): " + request.getUserId());
        System.out.println("addMember - isSelfJoin: " + isSelfJoin);
        System.out.println("addMember - team privacy: " + team.getPrivacy());
        
        if (isSelfJoin) {
            // Self-join: only allowed for PUBLIC teams
            if (team.getPrivacy() != Team.PrivacyType.PUBLIC) {
                throw new BadRequestException("Cannot join private team. You need an invitation.");
            }
        } else {
            // Adding someone else: require creator or PI permission
            TeamMember requesterMember = teamMemberRepository.findMemberInTeam(teamId, userId)
                    .orElse(null);
            
            boolean isCreator = team.getCreatedBy().getId().equals(userId);
            boolean isPI = requesterMember != null && requesterMember.getRole() == TeamMember.TeamRole.PI;

            if (!isCreator && !isPI) {
                throw new BadRequestException("Only team creator or PI members can add other members");
            }
        }

        TeamMember newMember = new TeamMember(team, userToAdd, request.getRole());
        TeamMember savedMember = teamMemberRepository.save(newMember);

        return mapToTeamMemberResponse(savedMember);
    }

    @Transactional
    public void removeMember(String teamId, String userId, String memberIdToRemove) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if user has permission (creator or PI member)
        TeamMember requesterMember = teamMemberRepository.findMemberInTeam(teamId, userId)
                .orElse(null);
        
        boolean isCreator = team.getCreatedBy().getId().equals(userId);
        boolean isPI = requesterMember != null && requesterMember.getRole() == TeamMember.TeamRole.PI;

        if (!isCreator && !isPI) {
            throw new BadRequestException("Only team creator or PI members can remove members");
        }

        // Cannot remove creator
        if (team.getCreatedBy().getId().equals(memberIdToRemove)) {
            throw new BadRequestException("Cannot remove team creator");
        }

        // Check if member exists
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, memberIdToRemove)) {
            throw new ResourceNotFoundException("Member not found in this team");
        }

        teamMemberRepository.deleteByTeamIdAndUserId(teamId, memberIdToRemove);
    }

    @Transactional
    public TeamMemberResponse updateMemberRole(String teamId, String userId, String memberIdToUpdate, UpdateMemberRoleRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check if user has permission (creator or PI member)
        TeamMember requesterMember = teamMemberRepository.findMemberInTeam(teamId, userId)
                .orElse(null);
        
        boolean isCreator = team.getCreatedBy().getId().equals(userId);
        boolean isPI = requesterMember != null && requesterMember.getRole() == TeamMember.TeamRole.PI;

        if (!isCreator && !isPI) {
            throw new BadRequestException("Only team creator or PI members can update member roles");
        }

        // Check if member exists
        TeamMember memberToUpdate = teamMemberRepository.findMemberInTeam(teamId, memberIdToUpdate)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this team"));

        // Cannot change creator's role
        if (team.getCreatedBy().getId().equals(memberIdToUpdate)) {
            throw new BadRequestException("Cannot change team creator's role");
        }

        // Update role
        memberToUpdate.setRole(request.getRole());
        TeamMember updatedMember = teamMemberRepository.save(memberToUpdate);
        
        return mapToTeamMemberResponse(updatedMember);
    }

    public List<TeamMemberResponse> getTeamMembers(String teamId, String userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        // Check access
        if (team.getPrivacy() == Team.PrivacyType.PRIVATE 
            && !team.getCreatedBy().getId().equals(userId)
            && !teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new BadRequestException("You do not have access to this team");
        }

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);
        return members.stream()
                .map(this::mapToTeamMemberResponse)
                .collect(Collectors.toList());
    }

    private TeamResponse mapToTeamResponse(Team team, String currentUserId) {
        TeamResponse response = new TeamResponse();
        response.setId(IdEncoder.encode(team.getId()));
        response.setName(team.getName());
        response.setDescription(team.getDescription());
        response.setResearchField(team.getResearchField());
        response.setPrivacy(team.getPrivacy());
        response.setIconUrl(team.getIconUrl());
        response.setCreatedDate(team.getCreatedDate());
        response.setUpdatedDate(team.getUpdatedDate());

        if (team.getCreatedBy() != null) {
            response.setCreatedById(IdEncoder.encode(team.getCreatedBy().getId()));
            response.setCreatedByName(team.getCreatedBy().getFullName());
            response.setCreatedByEmail(team.getCreatedBy().getEmail());
        }

        // Get member count
        int memberCount = teamMemberRepository.findByTeamId(team.getId()).size();
        response.setMemberCount(memberCount);

        // Check if current user is a member
        boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(team.getId(), currentUserId);
        response.setIsMember(isMember);

        // Get current user's role if member
        if (isMember) {
            teamMemberRepository.findMemberInTeam(team.getId(), currentUserId)
                    .ifPresent(member -> response.setCurrentUserRole(mapToTeamMemberResponse(member)));
        }

        return response;
    }

    private TeamMemberResponse mapToTeamMemberResponse(TeamMember member) {
        TeamMemberResponse response = new TeamMemberResponse();
        response.setId(IdEncoder.encode(member.getId()));
        
        if (member.getUser() != null) {
            response.setUserId(IdEncoder.encode(member.getUser().getId()));
            response.setUserName(member.getUser().getUsername());
            response.setUserFullName(member.getUser().getFullName());
            response.setUserEmail(member.getUser().getEmail());
            response.setUserAvatarUrl(member.getUser().getAvatarUrl());
        }
        
        response.setRole(member.getRole());
        response.setJoinedDate(member.getJoinedDate());
        
        return response;
    }
}

