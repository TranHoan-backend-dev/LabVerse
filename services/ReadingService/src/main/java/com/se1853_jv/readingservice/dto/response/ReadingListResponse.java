package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ReadingListResponse {
    private UUID id;
    private String name;
    private List<String> userIdsList;
    private List<String> paperIdsList;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

