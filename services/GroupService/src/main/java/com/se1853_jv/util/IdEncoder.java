package com.se1853_jv.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class IdEncoder {
    public static String encode(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        try {
            return Base64.getUrlEncoder().encodeToString(id.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return id;
        }
    }

    public static String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return encoded;
        }
        try {
            // First check if it's already a valid UUID (not encoded)
            if (encoded.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                return encoded;
            }
            
            // Check if it looks like Base64 URL-safe (contains only valid Base64 URL-safe characters)
            // Base64 URL-safe uses: A-Z, a-z, 0-9, -, _
            if (!encoded.matches("^[A-Za-z0-9_-]+$")) {
                // Not a valid Base64 string, assume it's already decoded or invalid
                return encoded;
            }
            
            // Try to decode as Base64 URL-safe
            // Base64.getUrlDecoder().decode() expects the Base64 string directly
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encoded);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            
            // Verify it's a valid UUID after decoding
            if (decoded.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                return decoded;
            }
            
            // If decode succeeded but not a UUID, return original (might be already decoded)
            return encoded;
        } catch (IllegalArgumentException e) {
            // Base64 decode failed - not a valid Base64 string, assume it's already decoded
            return encoded;
        } catch (Exception e) {
            // Other errors - return original
            return encoded;
        }
    }
}