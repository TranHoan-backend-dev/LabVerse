package com.se1853_jv.dto.response;

import java.time.LocalDateTime;

public class WrapperApiResponse {
    private int status;
    private String message;
    private Object data;
    private LocalDateTime timestamp;

    // Constructors
    public WrapperApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public WrapperApiResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    public WrapperApiResponse(int status, String message, Object data, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    // Getters
    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setStatus(int status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // Static factory methods
    public static WrapperApiResponse success(Object data) {
        return new WrapperApiResponse(200, "Success", data);
    }

    public static WrapperApiResponse success(String message, Object data) {
        return new WrapperApiResponse(200, message, data);
    }

    public static WrapperApiResponse error(int status, String message) {
        return new WrapperApiResponse(status, message, null);
    }
}

