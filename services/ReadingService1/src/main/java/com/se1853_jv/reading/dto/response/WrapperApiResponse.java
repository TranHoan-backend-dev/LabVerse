package com.se1853_jv.reading.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrapperApiResponse {
    private int status;
    private String message;
    private Object data;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static WrapperApiResponse success(Object data) {
        return WrapperApiResponse.builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static WrapperApiResponse error(int code, String message) {
        return WrapperApiResponse.builder()
                .status(code)
                .message(message)
                .data(null)
                .build();
    }
}

