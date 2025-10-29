package com.se1853_jv.controller;

import com.se1853_jv.dto.request.ForgotPasswordRequest;
import com.se1853_jv.dto.request.GoogleLoginRequest;
import com.se1853_jv.dto.request.LoginRequest;
import com.se1853_jv.dto.request.RegisterRequest;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<WrapperApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(WrapperApiResponse.success(authService.register(registerRequest)));
    }

    @PostMapping("/login")
    public ResponseEntity<WrapperApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(WrapperApiResponse.success(authService.login(loginRequest)));
    }

    @PostMapping("/google")
    public ResponseEntity<WrapperApiResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest googleLoginRequest) {
        return ResponseEntity.ok(WrapperApiResponse.success(authService.googleLogin(googleLoginRequest)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<WrapperApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(WrapperApiResponse.success("New password has been sent to your email", null));
    }

    @GetMapping("/health")
    public ResponseEntity<WrapperApiResponse> health() {
        return ResponseEntity.ok(WrapperApiResponse.success("Account Service is running", null));
    }
}

