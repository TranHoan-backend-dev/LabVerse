package com.se1853_jv.readingservice.service;

import com.se1853_jv.readingservice.dto.response.CollectionProgressStatisticsResponse;
import com.se1853_jv.readingservice.dto.response.PaperProgressResponse;
import com.se1853_jv.readingservice.dto.response.TeamMemberProgressResponse;

public interface ProgressTrackingService {

    /**
     * Get aggregated progress statistics for a collection
     * This is the main dashboard endpoint for PIs to view team progress
     */
    CollectionProgressStatisticsResponse getCollectionProgressStatistics(String collectionId);

    /**
     * Get progress statistics for a specific team member in a collection
     */
    TeamMemberProgressResponse getTeamMemberProgress(String collectionId, String userId);

    /**
     * Get progress statistics for a specific paper in a collection
     * Shows how all team members are progressing with this paper
     */
    PaperProgressResponse getPaperProgress(String collectionId, String paperId);
}













