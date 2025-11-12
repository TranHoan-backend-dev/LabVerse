package com.se1853_jv.controller;

import com.se1853_jv.dto.request.AddTeamMemberRequest;
import com.se1853_jv.dto.request.CreateTeamRequest;
import com.se1853_jv.dto.request.UpdateTeamRequest;
import com.se1853_jv.dto.request.UpdateMemberRoleRequest;
import com.se1853_jv.dto.response.TeamMemberResponse;
import com.se1853_jv.dto.response.TeamResponse;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.model.Team;
import com.se1853_jv.security.UserPrincipal;
import com.se1853_jv.service.TeamService;
import com.se1853_jv.util.IdEncoder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> createTeam(
            Authentication authentication,
            @Valid @RequestBody CreateTeamRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        TeamResponse team = teamService.createTeam(userPrincipal.getId(), request);
        return ResponseEntity.ok(WrapperApiResponse.success("Team created successfully", team));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> getTeamById(
            Authentication authentication,
            @PathVariable String id) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid team ID format"));
        }
        TeamResponse team = teamService.getTeamById(decodedId, userPrincipal.getId());
        return ResponseEntity.ok(WrapperApiResponse.success(team));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> getTeams(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String researchField,
            @RequestParam(required = false) String privacy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Team.PrivacyType privacyType = null;
        if (privacy != null && !privacy.isEmpty()) {
            try {
                privacyType = Team.PrivacyType.valueOf(privacy.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(WrapperApiResponse.error(400, "Invalid privacy type. Use PUBLIC or PRIVATE"));
            }
        }

        Page<TeamResponse> teams = teamService.getTeams(
                userPrincipal.getId(), 
                search, 
                researchField, 
                privacyType, 
                page, 
                size);
        
        return ResponseEntity.ok(WrapperApiResponse.success(teams));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> updateTeam(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody UpdateTeamRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid team ID format"));
        }
        TeamResponse team = teamService.updateTeam(decodedId, userPrincipal.getId(), request);
        return ResponseEntity.ok(WrapperApiResponse.success("Team updated successfully", team));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> deleteTeam(
            Authentication authentication,
            @PathVariable String id) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid team ID format"));
        }
        teamService.deleteTeam(decodedId, userPrincipal.getId());
        return ResponseEntity.ok(WrapperApiResponse.success("Team deleted successfully", null));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> addMember(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody AddTeamMemberRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid team ID format"));
        }
        
        // Decode user ID if encoded
        String decodedUserId = IdEncoder.decode(request.getUserId());
        if (decodedUserId == null) {
            decodedUserId = request.getUserId(); // Try using as-is if not encoded
        }
        
        // Log for debugging
        System.out.println("addMember Controller - userPrincipal.getId(): " + userPrincipal.getId());
        System.out.println("addMember Controller - request.getUserId() (original): " + request.getUserId());
        System.out.println("addMember Controller - decodedUserId: " + decodedUserId);
        
        AddTeamMemberRequest decodedRequest = new AddTeamMemberRequest();
        decodedRequest.setUserId(decodedUserId);
        decodedRequest.setRole(request.getRole());
        
        TeamMemberResponse member = teamService.addMember(decodedId, userPrincipal.getId(), decodedRequest);
        return ResponseEntity.ok(WrapperApiResponse.success("Member added successfully", member));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> removeMember(
            Authentication authentication,
            @PathVariable String id,
            @PathVariable String memberId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String decodedTeamId = IdEncoder.decode(id);
        String decodedMemberId = IdEncoder.decode(memberId);
        
        if (decodedTeamId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid team ID format"));
        }
        if (decodedMemberId == null) {
            decodedMemberId = memberId; // Try using as-is if not encoded
        }
        
        teamService.removeMember(decodedTeamId, userPrincipal.getId(), decodedMemberId);
        return ResponseEntity.ok(WrapperApiResponse.success("Member removed successfully", null));
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> getTeamMembers(
            Authentication authentication,
            @PathVariable String id) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid team ID format"));
        }
        java.util.List<TeamMemberResponse> members = teamService.getTeamMembers(decodedId, userPrincipal.getId());
        return ResponseEntity.ok(WrapperApiResponse.success(members));
    }

    @PutMapping("/{id}/members/{memberId}/role")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> updateMemberRole(
            Authentication authentication,
            @PathVariable String id,
            @PathVariable String memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String decodedTeamId = IdEncoder.decode(id);
        String decodedMemberId = IdEncoder.decode(memberId);
        
        if (decodedTeamId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid team ID format"));
        }
        if (decodedMemberId == null) {
            decodedMemberId = memberId; // Try using as-is if not encoded
        }
        
        TeamMemberResponse member = teamService.updateMemberRole(decodedTeamId, userPrincipal.getId(), decodedMemberId, request);
        return ResponseEntity.ok(WrapperApiResponse.success("Member role updated successfully", member));
    }
}

