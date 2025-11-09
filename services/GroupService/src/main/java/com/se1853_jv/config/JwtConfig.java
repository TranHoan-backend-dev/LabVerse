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
    
    public JwtConfig(@Value("${jwt.secret:5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Extract userId from JWT token
     * Token format from AccountService: subject = userId (String)
     */
    public String extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Subject in token is userId (String)
            return claims.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired token: " + e.getMessage());
        }
    }
    
    /**
     * Validate token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

