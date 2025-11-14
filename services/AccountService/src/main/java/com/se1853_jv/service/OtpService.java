package com.se1853_jv.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom random = new SecureRandom();

    /**
     * Generate a random 6-digit OTP code
     */
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Calculate OTP expiry time (10 minutes from now)
     */
    public LocalDateTime calculateExpiryTime() {
        return LocalDateTime.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES);
    }

    /**
     * Check if OTP is expired
     */
    public boolean isOtpExpired(LocalDateTime expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Verify OTP code
     */
    public boolean verifyOtp(String storedOtp, String providedOtp, LocalDateTime expiryTime) {
        if (storedOtp == null || providedOtp == null) {
            return false;
        }
        if (isOtpExpired(expiryTime)) {
            return false;
        }
        return storedOtp.equals(providedOtp);
    }
}

