package com.se1853_jv.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WrapperApiResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp = LocalDateTime.now();
}
