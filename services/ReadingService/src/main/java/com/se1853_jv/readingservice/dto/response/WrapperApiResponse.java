package com.se1853_jv.readingservice.dto.response;

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
public class WrapperApiResponse<T> {
    private int status;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> WrapperApiResponse<T> success(T data) {
        return WrapperApiResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> WrapperApiResponse<T> error(int code, String message) {
        return WrapperApiResponse.<T>builder()
                .status(code)
                .message(message)
                .data(null)
                .build();
    }
}
