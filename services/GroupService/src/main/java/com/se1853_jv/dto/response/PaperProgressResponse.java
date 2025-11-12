package com.se1853_jv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperProgressResponse {
    private String paperId; // Encoded
    private Long totalReaders;
    private Long unreadCount;
    private Long readingCount;
    private Long finishedCount;
    private Double averageProgress;
    private List<UserPaperProgressResponse> userProgressList;
}

