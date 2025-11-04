package com.se1853_jv.readingservice.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Utility class for encoding and decoding IDs to prevent direct exposure of internal IDs
 */
public class IdEncoder {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    /**
     * Encode a UUID to a base64 URL-safe string
     * @param id UUID to encode
     * @return Encoded string
     */
    public static String encode(UUID id) {
        if (id == null) {
            return null;
        }
        // Convert UUID to bytes (16 bytes)
        long mostSigBits = id.getMostSignificantBits();
        long leastSigBits = id.getLeastSignificantBits();
        byte[] bytes = new byte[16];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSigBits >>> (8 * (7 - i)));
        }
        for (int i = 8; i < 16; i++) {
            bytes[i] = (byte) (leastSigBits >>> (8 * (7 - i)));
        }
        return encoder.encodeToString(bytes);
    }

    /**
     * Encode a String ID to a base64 URL-safe string
     * @param id String ID to encode
     * @return Encoded string
     */
    public static String encode(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return encoder.encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decode an encoded UUID string back to UUID
     * @param encoded Encoded string
     * @return Decoded UUID
     */
    public static UUID decodeUuid(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = decoder.decode(encoded);
            if (bytes.length != 16) {
                throw new IllegalArgumentException("Invalid encoded UUID length");
            }
            long mostSigBits = 0;
            long leastSigBits = 0;
            for (int i = 0; i < 8; i++) {
                mostSigBits = (mostSigBits << 8) | (bytes[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                leastSigBits = (leastSigBits << 8) | (bytes[i] & 0xff);
            }
            return new UUID(mostSigBits, leastSigBits);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid encoded UUID: " + encoded, e);
        }
    }

    /**
     * Decode an encoded string ID back to original string
     * @param encoded Encoded string
     * @return Decoded string
     */
    public static String decodeString(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = decoder.decode(encoded);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid encoded ID: " + encoded, e);
        }
    }
}

