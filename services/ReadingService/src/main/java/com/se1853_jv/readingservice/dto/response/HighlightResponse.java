package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class HighlightResponse {
    private UUID id;
    private String color;
    private Integer coordinationX;
    private Integer coordinationY;
    private Integer pageNumber;
    private LocalDateTime createdAt;
}

