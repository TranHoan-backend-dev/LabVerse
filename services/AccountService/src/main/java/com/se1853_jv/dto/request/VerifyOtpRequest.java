package com.se1853_jv.dto.request;

import jakarta.validation.constraints.NotBlank;

public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP code is required")
    private String otpCode;

    public VerifyOtpRequest() {
    }

    public VerifyOtpRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}

