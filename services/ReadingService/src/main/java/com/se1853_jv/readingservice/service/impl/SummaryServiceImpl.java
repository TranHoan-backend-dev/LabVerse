package com.se1853_jv.readingservice.service.impl;

import com.se1853_jv.readingservice.dto.response.AnnotationsResponse;
import com.se1853_jv.readingservice.dto.response.HighlightResponse;
import com.se1853_jv.readingservice.dto.response.NoteResponse;
import com.se1853_jv.readingservice.dto.response.ReadingSummaryResponse;
import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.repository.ReadingWorkflowRepository;
import com.se1853_jv.readingservice.service.HighlightService;
import com.se1853_jv.readingservice.service.NoteService;
import com.se1853_jv.readingservice.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SummaryServiceImpl implements SummaryService {

    private final ReadingWorkflowRepository readingWorkflowRepository;
    private final NoteService noteService;
    private final HighlightService highlightService;

    @Override
    public ReadingSummaryResponse getReadingSummary(String userId) {
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findById_UserId(userId);

        long unread = workflows.stream()
                .filter(w -> "unread".equals(w.getStatus()))
                .count();

        long reading = workflows.stream()
                .filter(w -> "reading".equals(w.getStatus()))
                .count();

        long finished = workflows.stream()
                .filter(w -> "finished".equals(w.getStatus()))
                .count();

        return ReadingSummaryResponse.builder()
                .unread(unread)
                .reading(reading)
                .finished(finished)
                .build();
    }

    @Override
    public AnnotationsResponse getAnnotations(String paperId, String userId) {
        // Get all notes and highlights for this paper and user
        // Find workflows for this paper+user to get collectionIds
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findById_UserId(userId)
                .stream()
                .filter(w -> paperId.equals(w.getId().getPaperId()))
                .collect(java.util.stream.Collectors.toList());

        if (workflows.isEmpty()) {
            return AnnotationsResponse.builder()
                    .notes(List.of())
                    .highlights(List.of())
                    .build();
        }

        // Collect all notes and highlights from all workflows (paper can be in multiple collections)
        List<NoteResponse> allNotes = new java.util.ArrayList<>();
        List<HighlightResponse> allHighlights = new java.util.ArrayList<>();

        for (ReadingWorkflow workflow : workflows) {
            String collectionId = workflow.getId().getCollectionId();
            allNotes.addAll(noteService.getNotes(collectionId, paperId, userId));
            allHighlights.addAll(highlightService.getHighlights(collectionId, paperId, userId));
        }

        return AnnotationsResponse.builder()
                .notes(allNotes)
                .highlights(allHighlights)
                .build();
    }
}

