package com.se1853_jv.readingservice.service.impl;

import com.se1853_jv.readingservice.dto.response.CollectionProgressStatisticsResponse;
import com.se1853_jv.readingservice.dto.response.PaperProgressResponse;
import com.se1853_jv.readingservice.dto.response.StatusCountResponse;
import com.se1853_jv.readingservice.dto.response.TeamMemberProgressResponse;
import com.se1853_jv.readingservice.dto.response.UserPaperProgressResponse;
import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.repository.ReadingWorkflowRepository;
import com.se1853_jv.readingservice.service.ProgressTrackingService;
import com.se1853_jv.readingservice.util.IdEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private final ReadingWorkflowRepository readingWorkflowRepository;

    @Override
    public CollectionProgressStatisticsResponse getCollectionProgressStatistics(String collectionId) {
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findById_CollectionId(collectionId);

        if (workflows.isEmpty()) {
            return CollectionProgressStatisticsResponse.builder()
                    .collectionId(IdEncoder.encode(collectionId))
                    .totalPapers(0L)
                    .totalUsers(0L)
                    .unreadCount(0L)
                    .readingCount(0L)
                    .finishedCount(0L)
                    .averageProgress(0.0)
                    .build();
        }

        // Calculate basic statistics
        long unreadCount = workflows.stream()
                .filter(w -> "unread".equals(w.getStatus()))
                .count();
        long readingCount = workflows.stream()
                .filter(w -> "reading".equals(w.getStatus()))
                .count();
        long finishedCount = workflows.stream()
                .filter(w -> "finished".equals(w.getStatus()))
                .count();

        // Get unique papers and users
        Set<String> uniquePapers = workflows.stream()
                .map(w -> w.getId().getPaperId())
                .collect(Collectors.toSet());
        Set<String> uniqueUsers = workflows.stream()
                .map(w -> w.getId().getUsersid())
                .collect(Collectors.toSet());

        // Calculate average progress
        double averageProgress = workflows.stream()
                .filter(w -> w.getProgress() != null)
                .mapToInt(ReadingWorkflow::getProgress)
                .average()
                .orElse(0.0);

        // Status distribution
        Map<String, Long> statusMap = workflows.stream()
                .collect(Collectors.groupingBy(
                        w -> w.getStatus() != null ? w.getStatus() : "unread",
                        Collectors.counting()
                ));
        long total = workflows.size();
        List<StatusCountResponse> statusDistribution = statusMap.entrySet().stream()
                .map(entry -> StatusCountResponse.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .percentage(total > 0 ? (entry.getValue().doubleValue() / total) * 100.0 : 0.0)
                        .build())
                .collect(Collectors.toList());

        // Team member progress
        List<TeamMemberProgressResponse> teamMemberProgress = uniqueUsers.stream()
                .map(userId -> calculateTeamMemberProgress(collectionId, userId, workflows))
                .collect(Collectors.toList());

        return CollectionProgressStatisticsResponse.builder()
                .collectionId(IdEncoder.encode(collectionId))
                .totalPapers((long) uniquePapers.size())
                .totalUsers((long) uniqueUsers.size())
                .unreadCount(unreadCount)
                .readingCount(readingCount)
                .finishedCount(finishedCount)
                .averageProgress(averageProgress)
                .statusDistribution(statusDistribution)
                .teamMemberProgress(teamMemberProgress)
                .build();
    }

    @Override
    public TeamMemberProgressResponse getTeamMemberProgress(String collectionId, String userId) {
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findByCollectionIdAndUsersid(collectionId, userId);

        if (workflows.isEmpty()) {
            return TeamMemberProgressResponse.builder()
                    .userId(IdEncoder.encode(userId))
                    .totalPapers(0L)
                    .unreadCount(0L)
                    .readingCount(0L)
                    .finishedCount(0L)
                    .averageProgress(0.0)
                    .totalProgress(0)
                    .build();
        }

        long unreadCount = workflows.stream()
                .filter(w -> "unread".equals(w.getStatus()))
                .count();
        long readingCount = workflows.stream()
                .filter(w -> "reading".equals(w.getStatus()))
                .count();
        long finishedCount = workflows.stream()
                .filter(w -> "finished".equals(w.getStatus()))
                .count();

        double averageProgress = workflows.stream()
                .filter(w -> w.getProgress() != null)
                .mapToInt(ReadingWorkflow::getProgress)
                .average()
                .orElse(0.0);

        int totalProgress = workflows.stream()
                .filter(w -> w.getProgress() != null)
                .mapToInt(ReadingWorkflow::getProgress)
                .sum();

        return TeamMemberProgressResponse.builder()
                .userId(IdEncoder.encode(userId))
                .totalPapers((long) workflows.size())
                .unreadCount(unreadCount)
                .readingCount(readingCount)
                .finishedCount(finishedCount)
                .averageProgress(averageProgress)
                .totalProgress(totalProgress)
                .build();
    }

    @Override
    public PaperProgressResponse getPaperProgress(String collectionId, String paperId) {
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findByCollectionIdAndPaperId(collectionId, paperId);

        if (workflows.isEmpty()) {
            return PaperProgressResponse.builder()
                    .paperId(IdEncoder.encode(paperId))
                    .totalReaders(0L)
                    .unreadCount(0L)
                    .readingCount(0L)
                    .finishedCount(0L)
                    .averageProgress(0.0)
                    .build();
        }

        long unreadCount = workflows.stream()
                .filter(w -> "unread".equals(w.getStatus()))
                .count();
        long readingCount = workflows.stream()
                .filter(w -> "reading".equals(w.getStatus()))
                .count();
        long finishedCount = workflows.stream()
                .filter(w -> "finished".equals(w.getStatus()))
                .count();

        double averageProgress = workflows.stream()
                .filter(w -> w.getProgress() != null)
                .mapToInt(ReadingWorkflow::getProgress)
                .average()
                .orElse(0.0);

        List<UserPaperProgressResponse> userProgressList = workflows.stream()
                .map(w -> UserPaperProgressResponse.builder()
                        .userId(IdEncoder.encode(w.getId().getUsersid()))
                        .status(w.getStatus())
                        .lastPage(w.getLastPage())
                        .progress(w.getProgress())
                        .lastUpdated(w.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return PaperProgressResponse.builder()
                .paperId(IdEncoder.encode(paperId))
                .totalReaders((long) workflows.size())
                .unreadCount(unreadCount)
                .readingCount(readingCount)
                .finishedCount(finishedCount)
                .averageProgress(averageProgress)
                .userProgressList(userProgressList)
                .build();
    }

    private TeamMemberProgressResponse calculateTeamMemberProgress(
            String collectionId, String userId, List<ReadingWorkflow> allWorkflows) {
        List<ReadingWorkflow> userWorkflows = allWorkflows.stream()
                .filter(w -> userId.equals(w.getId().getUsersid()))
                .collect(Collectors.toList());

        if (userWorkflows.isEmpty()) {
            return TeamMemberProgressResponse.builder()
                    .userId(IdEncoder.encode(userId))
                    .totalPapers(0L)
                    .unreadCount(0L)
                    .readingCount(0L)
                    .finishedCount(0L)
                    .averageProgress(0.0)
                    .totalProgress(0)
                    .build();
        }

        long unreadCount = userWorkflows.stream()
                .filter(w -> "unread".equals(w.getStatus()))
                .count();
        long readingCount = userWorkflows.stream()
                .filter(w -> "reading".equals(w.getStatus()))
                .count();
        long finishedCount = userWorkflows.stream()
                .filter(w -> "finished".equals(w.getStatus()))
                .count();

        double averageProgress = userWorkflows.stream()
                .filter(w -> w.getProgress() != null)
                .mapToInt(ReadingWorkflow::getProgress)
                .average()
                .orElse(0.0);

        int totalProgress = userWorkflows.stream()
                .filter(w -> w.getProgress() != null)
                .mapToInt(ReadingWorkflow::getProgress)
                .sum();

        return TeamMemberProgressResponse.builder()
                .userId(IdEncoder.encode(userId))
                .totalPapers((long) userWorkflows.size())
                .unreadCount(unreadCount)
                .readingCount(readingCount)
                .finishedCount(finishedCount)
                .averageProgress(averageProgress)
                .totalProgress(totalProgress)
                .build();
    }
}















