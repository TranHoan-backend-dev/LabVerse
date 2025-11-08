package com.se1853_jv.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import javax.crypto.SecretKey

/**
 * JWT configuration để extract userId từ token
 * Sử dụng cùng secret key với AccountService
 */
@Component
class JwtConfig(
    @Value("\${jwt.secret:your_secret_key_sieu_dai_va_an_toan_o_day}")
    private val jwtSecret: String
) {
    
    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))
    }
    
    /**
     * Extract userId từ JWT token
     * Token format từ AccountService: subject = userId (String)
     */
    fun extractUserIdFromToken(token: String): String {
        return try {
            val claims: Claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .payload
            
            // Subject trong token là userId (String)
            claims.subject
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid or expired token: ${e.message}")
        }
    }
    
    /**
     * Validate token
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }
}







