package com.se1853_jv.controller;

import com.se1853_jv.dto.request.ForgotPasswordRequest;
import com.se1853_jv.dto.request.GoogleLoginRequest;
import com.se1853_jv.dto.request.LoginRequest;
import com.se1853_jv.dto.request.RegisterRequest;
import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
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

    @PostMapping("/logout")
    public ResponseEntity<WrapperApiResponse> logout() {
        authService.logout();
        return ResponseEntity.ok(WrapperApiResponse.success("Logout successful", null));
    }

    @RequestMapping(value = "/health", method = RequestMethod.HEAD)
    public ResponseEntity<Void> health() {
        logger.info("HEALTHY");
        return ResponseEntity.ok().build();
    }
}

