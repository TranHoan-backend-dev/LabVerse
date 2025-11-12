package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PaperProgressResponse {
    private String paperId; // Encoded
    private Long totalReaders;
    private Long unreadCount;
    private Long readingCount;
    private Long finishedCount;
    private Double averageProgress; // Average progress across all readers
    private List<UserPaperProgressResponse> userProgressList;
}






















