package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReadingWorkflowResponse {
    private String collectionId; // Encoded ID
    private String paperId; // Encoded ID
    private String userId; // Encoded ID
    private String status;
    private Integer lastPage;
    private Integer progress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

