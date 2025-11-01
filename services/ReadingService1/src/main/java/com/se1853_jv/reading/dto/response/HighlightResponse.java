package com.se1853_jv.reading.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HighlightResponse {
    private String id;
    private String color;
    private Integer coordinationX;
    private Integer coordinationY;
    private Integer pageNumber;
    private LocalDateTime createdAt;
}

