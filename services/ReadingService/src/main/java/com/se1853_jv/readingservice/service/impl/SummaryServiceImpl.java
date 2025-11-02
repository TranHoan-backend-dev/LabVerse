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
    public ReadingSummaryResponse getReadingSummary(String usersid) {
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findById_Usersid(usersid);

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
    public AnnotationsResponse getAnnotations(String paperId, String usersid) {
        // Get all notes and highlights for this paper and user
        // Find workflows for this paper+user
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findById_Usersid(usersid)
                .stream()
                .filter(w -> paperId.equals(w.getId().getPaperId()))
                .collect(java.util.stream.Collectors.toList());

        // Collect notes and highlights directly from Many-to-Many relationships
        List<NoteResponse> allNotes = new java.util.ArrayList<>();
        List<HighlightResponse> allHighlights = new java.util.ArrayList<>();

        for (ReadingWorkflow workflow : workflows) {
            if (workflow.getNotes() != null) {
                allNotes.addAll(workflow.getNotes().stream()
                        .map(n -> NoteResponse.builder()
                                .id(com.se1853_jv.readingservice.util.IdEncoder.encode(n.getId()))
                                .content(n.getContent())
                                .coordinationX(n.getCoordinationX())
                                .coordinationY(n.getCoordinationY())
                                .pageNumber(n.getPageNumber())
                                .createdAt(n.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()));
            }
            if (workflow.getHighlights() != null) {
                allHighlights.addAll(workflow.getHighlights().stream()
                        .map(h -> HighlightResponse.builder()
                                .id(com.se1853_jv.readingservice.util.IdEncoder.encode(h.getId()))
                                .color(h.getColor())
                                .coordinationX(h.getCoordinationX())
                                .coordinationY(h.getCoordinationY())
                                .pageNumber(h.getPageNumber())
                                .createdAt(h.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()));
            }
        }

        return AnnotationsResponse.builder()
                .notes(allNotes)
                .highlights(allHighlights)
                .build();
    }
}

