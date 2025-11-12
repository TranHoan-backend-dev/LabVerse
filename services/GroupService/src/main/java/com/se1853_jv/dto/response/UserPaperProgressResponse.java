package com.se1853_jv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPaperProgressResponse {
    private String userId; // Encoded
    private String status;
    private Integer lastPage;
    private Integer progress;
    private LocalDateTime lastUpdated;
}

