package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserPaperProgressResponse {
    private String userId; // Encoded
    private String status;
    private Integer lastPage;
    private Integer progress;
    private LocalDateTime lastUpdated;
}

















