package com.se1853_jv.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.se1853_jv.dto.request.GoogleLoginRequest;
import com.se1853_jv.dto.request.LoginRequest;
import com.se1853_jv.dto.request.RegisterRequest;
import com.se1853_jv.dto.request.VerifyOtpRequest;
import com.se1853_jv.dto.response.AuthResponse;
import com.se1853_jv.exception.BadRequestException;
import com.se1853_jv.exception.ResourceNotFoundException;
import com.se1853_jv.model.Role;
import com.se1853_jv.model.User;
import com.se1853_jv.repository.RoleRepository;
import com.se1853_jv.repository.UserRepository;
import com.se1853_jv.security.JwtTokenProvider;
import com.se1853_jv.security.UserPrincipal;
import com.se1853_jv.util.IdEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final GoogleOAuth2Service googleOAuth2Service;
    private final EmailService emailService;
    private final OtpService otpService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                      UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider,
                      GoogleOAuth2Service googleOAuth2Service,
                      EmailService emailService,
                      OtpService otpService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.googleOAuth2Service = googleOAuth2Service;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email address already in use");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username already in use");
        }

        // Create new user
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setFullName(registerRequest.getFullName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        // Set role
        Role role = roleRepository.findByName(registerRequest.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + registerRequest.getRoleName()));
        user.setRole(role);

        // Generate and set OTP
        String otpCode = otpService.generateOtp();
        user.setOtpCode(otpCode);
        user.setOtpExpiresAt(otpService.calculateExpiryTime());
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        // Send OTP email
        try {
            emailService.sendOtpEmail(savedUser.getEmail(), savedUser.getFullName(), otpCode);
        } catch (Exception e) {
            throw new BadRequestException("Failed to send verification email: " + e.getMessage());
        }
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        // Find user by email
        User user = userRepository.findByEmail(verifyOtpRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found with email: " + verifyOtpRequest.getEmail()));

        // Verify OTP
        if (!otpService.verifyOtp(user.getOtpCode(), verifyOtpRequest.getOtpCode(), user.getOtpExpiresAt())) {
            throw new BadRequestException("Invalid or expired OTP code");
        }

        // Mark email as verified and clear OTP
        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        user.setIsActive(true);
        userRepository.save(user);

        // Generate JWT token
        String token = tokenProvider.generateTokenFromUserId(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getRole().getName()
        );

        return new AuthResponse(
            token,
            IdEncoder.encode(user.getId()),
            user.getEmail(),
            user.getUsername(),
            user.getFullName(),
            user.getAvatarUrl(),
            user.getRole().getName()
        );
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if email is verified
        if (user.getEmailVerified() == null || !user.getEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in. Check your inbox for the verification code.");
        }

        return new AuthResponse(
            token,
            IdEncoder.encode(user.getId()),
            user.getEmail(),
            user.getUsername(),
            user.getFullName(),
            user.getAvatarUrl(),
            user.getRole().getName()
        );
    }

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest googleLoginRequest) {
        try {
            // Verify Google token
            GoogleIdToken.Payload payload = googleOAuth2Service.verifyGoogleToken(googleLoginRequest.getIdToken());
            
            if (payload == null) {
                throw new BadRequestException("Invalid Google ID token");
            }

            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // Check if user exists
            User user = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> userRepository.findByEmail(email)
                            .orElse(null));

            if (user == null) {
                // Create new user
                user = new User();
                user.setEmail(email);
                user.setGoogleId(googleId);
                user.setFullName(name);
                user.setUsername(email.split("@")[0]); // Use email prefix as username
                user.setAvatarUrl(pictureUrl);
                // Google users are automatically verified
                user.setEmailVerified(true);

                // Set default role as RESEARCHER
                Role role = roleRepository.findByName("RESEARCHER")
                        .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
                user.setRole(role);

                user = userRepository.save(user);
            } else {
                // Update existing user if needed
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                }
                if (user.getAvatarUrl() == null || !user.getAvatarUrl().equals(pictureUrl)) {
                    user.setAvatarUrl(pictureUrl);
                }
                // Mark as verified if not already
                if (user.getEmailVerified() == null || !user.getEmailVerified()) {
                    user.setEmailVerified(true);
                }
                user = userRepository.save(user);
            }

            // Generate JWT token
            String token = tokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().getName()
            );

            return new AuthResponse(
                token,
                IdEncoder.encode(user.getId()),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRole().getName()
            );

        } catch (GeneralSecurityException | IOException e) {
            throw new BadRequestException("Failed to verify Google token: " + e.getMessage());
        }
    }

    @Transactional
    public void forgotPassword(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email not found in system"));

        // Generate new random password
        String newPassword = generateRandomPassword();

        // Update user password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Send email with new password
        try {
            emailService.sendNewPassword(user.getEmail(), user.getFullName(), newPassword);
        } catch (Exception e) {
            throw new BadRequestException("Failed to send email: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }

    public void logout() {
        // Clear the security context
        SecurityContextHolder.clearContext();
    }
}

