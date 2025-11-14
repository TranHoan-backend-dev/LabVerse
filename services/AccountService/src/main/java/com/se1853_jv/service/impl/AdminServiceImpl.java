package com.se1853_jv.service.impl;

import com.se1853_jv.dto.response.AdminUserDetailsResponse;
import com.se1853_jv.dto.response.OverviewStatisticsResponse;
import com.se1853_jv.dto.response.TeamResponse;
import com.se1853_jv.dto.response.UserResponse;
import com.se1853_jv.exception.BadRequestException;
import com.se1853_jv.exception.ResourceNotFoundException;
import com.se1853_jv.model.Role;
import com.se1853_jv.model.Team;
import com.se1853_jv.model.User;
import com.se1853_jv.repository.RoleRepository;
import com.se1853_jv.repository.TeamMemberRepository;
import com.se1853_jv.repository.TeamRepository;
import com.se1853_jv.repository.UserRepository;
import com.se1853_jv.service.AdminService;
import com.se1853_jv.util.IdEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           TeamRepository teamRepository,
                           TeamMemberRepository teamMemberRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String search, String role, Boolean isActive) {
        Pageable pageable = PageRequest.of(page, size);
        // Normalize empty strings to null
        String normalizedSearch = (search != null && search.trim().isEmpty()) ? null : search;
        String normalizedRole = (role != null && role.trim().isEmpty()) ? null : role;
        Page<User> users = userRepository.findAllWithFilters(normalizedSearch, normalizedRole, isActive, pageable);
        return users.map(this::mapToUserResponse);
    }

    @Override
    public AdminUserDetailsResponse getUserDetails(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Count user's papers (would need to call PaperService - for now return 0)
        Long paperCount = 0L; // TODO: Call PaperService to get actual count
        
        // Count user's teams
        Long teamCount = (long) teamMemberRepository.findByUserId(userId).size();
        
        // Count user's collections (would need to call GroupService - for now return 0)
        Long collectionCount = 0L; // TODO: Call GroupService to get actual count

        return new AdminUserDetailsResponse(
                IdEncoder.encode(user.getId()),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRole().getName(),
                user.getIsActive() != null ? user.getIsActive() : true,
                user.getCreatedDate(),
                user.getUpdatedDate(),
                paperCount,
                teamCount,
                collectionCount
        );
    }

    @Override
    @Transactional
    public void activateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse changeUserRole(String userId, String roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        // Prevent changing own role
        // Note: This check should be done in controller using current user's ID
        
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Soft delete - mark as inactive
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public Page<TeamResponse> getAllTeams(int page, int size, String search, String privacy) {
        Pageable pageable = PageRequest.of(page, size);
        
        Team.PrivacyType privacyType = null;
        if (privacy != null && !privacy.isEmpty()) {
            try {
                privacyType = Team.PrivacyType.valueOf(privacy.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid privacy type: " + privacy);
            }
        }

        // For admin, show all teams regardless of privacy
        Page<Team> teams = teamRepository.findAllForAdmin(search, privacyType, pageable);
        return teams.map(this::mapToTeamResponse);
    }

    @Override
    @Transactional
    public void deleteTeam(String teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        
        // Delete team members first
        teamMemberRepository.deleteAll(teamMemberRepository.findByTeamId(teamId));
        
        // Delete team
        teamRepository.delete(team);
    }

    @Override
    public OverviewStatisticsResponse getOverviewStatistics() {
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countActiveUsers();
        Long inactiveUsers = userRepository.countInactiveUsers();
        
        Long totalTeams = teamRepository.count();
        Long publicTeams = teamRepository.countPublicTeams() != null ? teamRepository.countPublicTeams() : 0L;
        Long privateTeams = teamRepository.countPrivateTeams() != null ? teamRepository.countPrivateTeams() : 0L;
        
        // Papers and collections would need to call other services
        Long totalPapers = 0L; // TODO: Call PaperService
        Long papersThisMonth = 0L; // TODO: Call PaperService
        Long totalCollections = 0L; // TODO: Call GroupService
        Long totalReadingLists = 0L; // TODO: Call ReadingService

        return new OverviewStatisticsResponse(
                totalUsers,
                activeUsers,
                inactiveUsers,
                totalPapers,
                papersThisMonth,
                totalTeams,
                publicTeams,
                privateTeams,
                totalCollections,
                totalReadingLists
        );
    }

    private UserResponse mapToUserResponse(User user) {
        // Check if user has role to avoid NullPointerException
        if (user.getRole() == null) {
            throw new IllegalStateException("User " + user.getEmail() + " does not have a role assigned");
        }
        
        return new UserResponse(
                IdEncoder.encode(user.getId()),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRole().getName(),
                user.getCreatedDate(),
                user.getUpdatedDate()
        );
    }

    private TeamResponse mapToTeamResponse(Team team) {
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
        response.setIsMember(false); // Admin view doesn't need this

        return response;
    }
}

