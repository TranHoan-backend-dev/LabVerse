package com.se1853_jv.readingservice.service.impl;

import com.se1853_jv.readingservice.dto.request.HighlightRequest;
import com.se1853_jv.readingservice.dto.response.HighlightResponse;
import com.se1853_jv.readingservice.exception.ResourceNotFoundException;
import com.se1853_jv.readingservice.model.Highlight;
import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.model.ReadingWorkflowId;
import com.se1853_jv.readingservice.repository.HighlightRepository;
import com.se1853_jv.readingservice.repository.ReadingWorkflowRepository;
import com.se1853_jv.readingservice.service.HighlightService;
import com.se1853_jv.readingservice.util.IdEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HighlightServiceImpl implements HighlightService {

    private final HighlightRepository highlightRepository;
    private final ReadingWorkflowRepository readingWorkflowRepository;

    @Override
    public HighlightResponse addHighlight(HighlightRequest request) {
        // Ensure workflow exists
        ReadingWorkflowId workflowId = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUsersid()
        );
        
        ReadingWorkflow workflow = readingWorkflowRepository.findById(workflowId)
                .orElseGet(() -> {
                    // Create workflow if not exists
                    ReadingWorkflow newWorkflow = ReadingWorkflow.builder()
                            .id(workflowId)
                            .status("reading")
                            .lastPage(request.getPageNumber())
                            .progress(0)
                            .build();
                    return readingWorkflowRepository.save(newWorkflow);
                });

        // Create highlight with UUID
        String highlightId = java.util.UUID.randomUUID().toString();
        Highlight highlight = Highlight.builder()
                .id(highlightId)
                .color(request.getColor())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .build();

        Highlight savedHighlight = highlightRepository.save(highlight);

        // Add highlight to workflow's highlights collection (Many-to-Many)
        if (workflow.getHighlights() == null) {
            workflow.setHighlights(new java.util.ArrayList<>());
        }
        workflow.getHighlights().add(savedHighlight);
        readingWorkflowRepository.save(workflow);

        return toResponse(savedHighlight);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HighlightResponse> getHighlights(String collectionId, String paperId, String usersid) {
        List<Highlight> highlights = highlightRepository.findByCollectionIdAndPaperIdAndUsersid(collectionId, paperId, usersid);
        return highlights.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHighlight(String highlightId) {
        // Check if highlight exists
        if (!highlightRepository.existsById(highlightId)) {
            throw new ResourceNotFoundException("Highlight not found");
        }
        
        // Remove highlight from all workflows
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findAll();
        for (ReadingWorkflow workflow : workflows) {
            if (workflow.getHighlights() != null) {
                workflow.getHighlights().removeIf(h -> h.getId().equals(highlightId));
                readingWorkflowRepository.save(workflow);
            }
        }
        
        // Delete highlight
        highlightRepository.deleteById(highlightId);
    }

    private HighlightResponse toResponse(Highlight highlight) {
        return HighlightResponse.builder()
                .id(IdEncoder.encode(highlight.getId()))
                .color(highlight.getColor())
                .coordinationX(highlight.getCoordinationX())
                .coordinationY(highlight.getCoordinationY())
                .pageNumber(highlight.getPageNumber())
                .createdAt(highlight.getCreatedAt())
                .build();
    }
}

