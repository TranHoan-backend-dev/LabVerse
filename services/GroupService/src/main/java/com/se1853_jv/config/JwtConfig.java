package com.se1853_jv.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT configuration to extract userId from token
 * Uses the same secret key as AccountService
 */
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
            return claims.getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new IllegalArgumentException("Token has expired");
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new IllegalArgumentException("Invalid token signature");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new IllegalArgumentException("Invalid token format");
        } catch (Exception e) {
            // Generic error - do not expose internal details
            throw new IllegalArgumentException("Invalid or expired token");
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

