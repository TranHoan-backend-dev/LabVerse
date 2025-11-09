package com.se1853_jv.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;
import java.nio.charset.StandardCharsets;
@Service
public class JwtService {

    @Value("${app.jwt.secret-key}")
    private String secretKey;

    // Trích xuất "Subject" (chính là userId UUID) từ token
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Trích xuất 1 claim cụ thể
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Kiểm tra token có hợp lệ không
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Kiểm tra token hết hạn chưa
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Hàm chính: Giải mã toàn bộ token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // Log lỗi chi tiết để debug
            System.err.println("Error parsing JWT token: " + e.getMessage());
            System.err.println("Token (first 50 chars): " + (token != null ? token.substring(0, Math.min(50, token.length())) + "..." : "null"));
            System.err.println("Secret key length: " + (secretKey != null ? secretKey.length() : 0));
            throw e;
        }
    }

    // Lấy secret key từ file properties
    private SecretKey getSigningKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}