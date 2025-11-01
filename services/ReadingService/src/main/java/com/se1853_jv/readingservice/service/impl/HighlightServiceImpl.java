package com.se1853_jv.readingservice.service.impl;

import com.se1853_jv.readingservice.dto.request.HighlightRequest;
import com.se1853_jv.readingservice.dto.response.HighlightResponse;
import com.se1853_jv.readingservice.exception.ResourceNotFoundException;
import com.se1853_jv.readingservice.model.Highlight;
import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.model.ReadingWorkflowHighlight;
import com.se1853_jv.readingservice.model.ReadingWorkflowHighlightId;
import com.se1853_jv.readingservice.model.ReadingWorkflowId;
import com.se1853_jv.readingservice.repository.HighlightRepository;
import com.se1853_jv.readingservice.repository.ReadingWorkflowHighlightRepository;
import com.se1853_jv.readingservice.repository.ReadingWorkflowRepository;
import com.se1853_jv.readingservice.service.HighlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HighlightServiceImpl implements HighlightService {

    private final HighlightRepository highlightRepository;
    private final ReadingWorkflowRepository readingWorkflowRepository;
    private final ReadingWorkflowHighlightRepository readingWorkflowHighlightRepository;

    @Override
    public HighlightResponse addHighlight(HighlightRequest request) {
        // Ensure workflow exists
        ReadingWorkflowId workflowId = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUserId()
        );
        
        if (!readingWorkflowRepository.existsById(workflowId)) {
            // Create workflow if not exists
            ReadingWorkflow workflow = ReadingWorkflow.builder()
                    .id(workflowId)
                    .status("reading")
                    .lastPage(request.getPageNumber())
                    .progress(0)
                    .build();
            readingWorkflowRepository.save(workflow);
        }

        // Create highlight (ID will be auto-generated)
        Highlight highlight = Highlight.builder()
                .color(request.getColor())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .build();

        Highlight savedHighlight = highlightRepository.save(highlight);

        // Create mapping
        ReadingWorkflowHighlightId mappingId = new ReadingWorkflowHighlightId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUserId(),
                savedHighlight.getId().toString()
        );

        ReadingWorkflowHighlight mapping = ReadingWorkflowHighlight.builder()
                .id(mappingId)
                .build();

        readingWorkflowHighlightRepository.save(mapping);

        return toResponse(savedHighlight);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HighlightResponse> getHighlights(String collectionId, String paperId, String userId) {
        List<Highlight> highlights = highlightRepository.findByCollectionIdAndPaperIdAndUserId(collectionId, paperId, userId);
        return highlights.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHighlight(UUID highlightId) {
        // Delete mappings first
        readingWorkflowHighlightRepository.deleteById_HighlightId(highlightId.toString());
        
        // Delete highlight
        highlightRepository.deleteById(highlightId);
    }

    private HighlightResponse toResponse(Highlight highlight) {
        return HighlightResponse.builder()
                .id(highlight.getId())
                .color(highlight.getColor())
                .coordinationX(highlight.getCoordinationX())
                .coordinationY(highlight.getCoordinationY())
                .pageNumber(highlight.getPageNumber())
                .createdAt(highlight.getCreatedAt())
                .build();
    }
}

