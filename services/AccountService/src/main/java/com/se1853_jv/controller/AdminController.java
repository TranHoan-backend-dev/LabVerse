package com.se1853_jv.controller;

import com.se1853_jv.dto.request.ChangeUserRoleRequest;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.exception.BadRequestException;
import com.se1853_jv.security.UserPrincipal;
import com.se1853_jv.service.AdminService;
import com.se1853_jv.util.IdEncoder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ========== USER MANAGEMENT ==========
    
    @GetMapping("/users")
    public ResponseEntity<WrapperApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getAllUsers(page, size, search, role, isActive)
        ));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<WrapperApiResponse> getUserDetails(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            throw new BadRequestException("Invalid user ID format");
        }
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getUserDetails(decodedId)
        ));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<WrapperApiResponse> activateUser(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            throw new BadRequestException("Invalid user ID format");
        }
        adminService.activateUser(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("User activated successfully", null));
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<WrapperApiResponse> deactivateUser(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            throw new BadRequestException("Invalid user ID format");
        }
        adminService.deactivateUser(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("User deactivated successfully", null));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<WrapperApiResponse> changeUserRole(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody ChangeUserRoleRequest request) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            throw new BadRequestException("Invalid user ID format");
        }
        
        // Prevent admin from changing their own role
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal.getId().equals(decodedId)) {
            throw new BadRequestException("Cannot change your own role");
        }
        
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.changeUserRole(decodedId, request.getRoleId())
        ));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<WrapperApiResponse> deleteUser(
            Authentication authentication,
            @PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            throw new BadRequestException("Invalid user ID format");
        }
        
        // Prevent admin from deleting themselves
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal.getId().equals(decodedId)) {
            throw new BadRequestException("Cannot delete your own account");
        }
        
        adminService.deleteUser(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("User deleted successfully", null));
    }

    // ========== TEAM MANAGEMENT ==========
    
    @GetMapping("/teams")
    public ResponseEntity<WrapperApiResponse> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String privacy) {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getAllTeams(page, size, search, privacy)
        ));
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<WrapperApiResponse> deleteTeam(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            throw new BadRequestException("Invalid team ID format");
        }
        adminService.deleteTeam(decodedId);
        return ResponseEntity.ok(WrapperApiResponse.success("Team deleted successfully", null));
    }

    // ========== STATISTICS ==========
    
    @GetMapping("/statistics/overview")
    public ResponseEntity<WrapperApiResponse> getOverviewStatistics() {
        return ResponseEntity.ok(WrapperApiResponse.success(
            adminService.getOverviewStatistics()
        ));
    }
}

