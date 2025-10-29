package com.se1853_jv.controller;

import com.se1853_jv.dto.request.ChangePasswordRequest;
import com.se1853_jv.dto.request.UpdateProfileRequest;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.exception.BadRequestException;
import com.se1853_jv.security.UserPrincipal;
import com.se1853_jv.service.UserService;
import com.se1853_jv.util.IdEncoder;
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
    public ResponseEntity<WrapperApiResponse> getCurrentUser(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(WrapperApiResponse.success(userService.getUserById(userPrincipal.getId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> getUserById(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            throw new BadRequestException("Invalid user ID format");
        }
        return ResponseEntity.ok(WrapperApiResponse.success(userService.getUserById(decodedId)));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(WrapperApiResponse.success(userService.updateProfile(userPrincipal.getId(), request)));
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        userService.changePassword(userPrincipal.getId(), request);
        return ResponseEntity.ok(WrapperApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WrapperApiResponse> deleteAccount(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        userService.deleteAccount(userPrincipal.getId());
        return ResponseEntity.ok(WrapperApiResponse.success("Account deleted successfully", null));
    }
}

