package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ReadingListResponse {
    private String id; // Encoded UUID
    private String name;
    private List<String> userIdsList; // Encoded user IDs
    private List<String> paperIdsList; // Encoded paper IDs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

