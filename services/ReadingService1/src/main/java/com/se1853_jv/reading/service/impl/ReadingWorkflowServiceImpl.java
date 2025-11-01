package com.se1853_jv.reading.service.impl;

import com.se1853_jv.reading.dto.request.ReadingWorkflowCreateRequest;
import com.se1853_jv.reading.dto.request.ReadingWorkflowUpdateRequest;
import com.se1853_jv.reading.dto.response.ReadingWorkflowResponse;
import com.se1853_jv.reading.exception.DatabaseException;
import com.se1853_jv.reading.exception.ResourceNotFoundException;
import com.se1853_jv.reading.model.ReadingWorkflow;
import com.se1853_jv.reading.model.ReadingWorkflowId;
import com.se1853_jv.reading.repository.ReadingWorkflowRepository;
import com.se1853_jv.reading.service.ReadingWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReadingWorkflowServiceImpl implements ReadingWorkflowService {

    private final ReadingWorkflowRepository readingWorkflowRepository;

    @Override
    public ReadingWorkflowResponse createWorkflow(ReadingWorkflowCreateRequest request) {
        ReadingWorkflowId id = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUserId()
        );

        if (readingWorkflowRepository.existsById(id)) {
            throw new DatabaseException("Reading workflow already exists for the given identifiers");
        }

        ReadingWorkflow workflow = ReadingWorkflow.builder()
                .id(id)
                .status(request.getStatus())
                .lastPage(request.getLastPage())
                .progress(request.getProgress())
                .build();

        ReadingWorkflow saved = readingWorkflowRepository.save(workflow);
        return toResponse(saved);
    }

    @Override
    public ReadingWorkflowResponse updateWorkflow(ReadingWorkflowUpdateRequest request) {
        ReadingWorkflowId id = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUserId()
        );

        ReadingWorkflow workflow = readingWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading workflow not found"));

        if (request.getStatus() != null) {
            workflow.setStatus(request.getStatus());
        }
        if (request.getLastPage() != null) {
            workflow.setLastPage(request.getLastPage());
        }
        if (request.getProgress() != null) {
            workflow.setProgress(request.getProgress());
        }

        ReadingWorkflow updated = readingWorkflowRepository.save(workflow);
        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ReadingWorkflowResponse getWorkflow(String collectionId, String paperId, String userId) {
        ReadingWorkflowId id = new ReadingWorkflowId(collectionId, paperId, userId);
        ReadingWorkflow workflow = readingWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading workflow not found"));
        return toResponse(workflow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingWorkflowResponse> getWorkflowsByUser(String userId) {
        return readingWorkflowRepository.findByIdUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReadingWorkflowResponse toResponse(ReadingWorkflow workflow) {
        ReadingWorkflowId id = workflow.getId();
        return ReadingWorkflowResponse.builder()
                .collectionId(id.getCollectionId())
                .paperId(id.getPaperId())
                .userId(id.getUserId())
                .status(workflow.getStatus())
                .lastPage(workflow.getLastPage())
                .progress(workflow.getProgress())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }
}

