package com.se1853_jv.controller;

import com.se1853_jv.dto.request.ChangePasswordRequest;
import com.se1853_jv.dto.request.UpdateProfileRequest;
import com.se1853_jv.dto.response.MessageResponse;
import com.se1853_jv.dto.response.UserResponse;
import com.se1853_jv.security.UserPrincipal;
import com.se1853_jv.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserResponse response = userService.getUserById(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserResponse response = userService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        userService.changePassword(userPrincipal.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> deleteAccount(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        userService.deleteAccount(userPrincipal.getId());
        return ResponseEntity.ok(new MessageResponse("Account deleted successfully"));
    }
}

