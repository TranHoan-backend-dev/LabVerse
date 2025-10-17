package com.se1853_jv.labverse.data.utils;

import android.os.Build;

import java.util.Arrays;
import java.util.Base64;

public class EncoderService {
    public static String encode(String str) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Arrays.toString(Base64.getUrlEncoder().encode(str.getBytes()));
        }
        return null;
    }

    public static String decode(String str) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Arrays.toString(Base64.getUrlDecoder().decode(str));
        }
        return null;
    }

}