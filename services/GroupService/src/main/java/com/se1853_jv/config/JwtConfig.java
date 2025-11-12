package com.se1853_jv.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT configuration to extract userId from token
 * Uses the same secret key as AccountService
 */
@Slf4j
@Component
public class JwtConfig {
    private final String jwtSecret;
    
    public JwtConfig(@Value("${jwt.secret}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT secret key is required. Please set jwt.secret in application.properties or JWT_SECRET environment variable."
            );
        }
        this.jwtSecret = jwtSecret;
        log.info("JwtConfig initialized with secret key length: {}", jwtSecret.length());
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Extract userId from JWT token
     * Token format from AccountService: subject = userId (String)
     * Note: Does not log token or secret for security reasons
     */
    public String extractUserIdFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token cannot be null or empty");
            }
            
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Subject in token is userId (String)
            String userId = claims.getSubject();
            log.debug("Successfully extracted userId from token: {}", userId != null ? userId.substring(0, Math.min(8, userId.length())) + "..." : "null");
            return userId;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT token has expired");
            throw new IllegalArgumentException("Token has expired");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("Invalid JWT token signature. Secret key length: {}, Token length: {}", 
                    jwtSecret != null ? jwtSecret.length() : 0, 
                    token != null ? token.length() : 0);
            throw new IllegalArgumentException("Invalid token signature");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token format");
        } catch (Exception e) {
            log.error("Error parsing JWT token: {}", e.getMessage(), e);
            // Generic error - do not expose internal details
            throw new IllegalArgumentException("Invalid or expired token: " + e.getMessage());
        }
    }
    
    /**
     * Validate token
     * Returns true if token is valid, false otherwise
     * Note: Does not log token or secret for security reasons
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            // Return false without logging sensitive information
            return false;
        }
    }
}

