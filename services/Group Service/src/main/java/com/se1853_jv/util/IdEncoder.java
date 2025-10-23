package com.se1853_jv.util;

import java.util.Base64;

public class IdEncoder {
    public static String encode(String id) {
        return Base64.getUrlEncoder().encodeToString(id.getBytes());
    }

    public static String decode(String encoded) {
        try {
            return new String(Base64.getUrlDecoder().decode(encoded));
        } catch (Exception e) {
            return null;
        }
    }
}