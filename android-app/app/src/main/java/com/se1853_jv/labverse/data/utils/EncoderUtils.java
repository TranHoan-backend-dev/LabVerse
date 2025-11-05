package com.se1853_jv.labverse.data.utils;

import android.os.Build;

import java.util.Base64;

public class EncoderUtils {
    public static String encode(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Use encodeToString to match backend IdEncoder.encode()
                return Base64.getUrlEncoder().encodeToString(str.getBytes());
            } catch (Exception e) {
                return str;
            }
        }
        return str;
    }

    public static String decode(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                byte[] decoded = Base64.getUrlDecoder().decode(str.getBytes());
                return new String(decoded);
            } catch (Exception e) {
                return str;
            }
        }
        return str;
    }

}