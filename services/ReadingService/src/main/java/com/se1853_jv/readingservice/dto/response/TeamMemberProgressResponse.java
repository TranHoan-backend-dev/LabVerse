package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TeamMemberProgressResponse {
    private String userId; // Encoded
    private Long totalPapers;
    private Long unreadCount;
    private Long readingCount;
    private Long finishedCount;
    private Double averageProgress; // Average progress for this user
    private Integer totalProgress; // Sum of all progress values
}

