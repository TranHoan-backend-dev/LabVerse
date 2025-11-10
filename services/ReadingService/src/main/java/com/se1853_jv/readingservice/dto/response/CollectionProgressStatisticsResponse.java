package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CollectionProgressStatisticsResponse {
    private String collectionId; // Encoded
    private Long totalPapers;
    private Long totalUsers;
    private Long unreadCount;
    private Long readingCount;
    private Long finishedCount;
    private Double averageProgress; // Average progress percentage across all workflows
    private List<StatusCountResponse> statusDistribution;
    private List<TeamMemberProgressResponse> teamMemberProgress;
}















