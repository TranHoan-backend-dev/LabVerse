package com.se1853_jv.readingservice.service.impl;

import com.se1853_jv.readingservice.dto.request.ReadingWorkflowCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowDeleteRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowProgressRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowStatusRequest;
import com.se1853_jv.readingservice.dto.response.ReadingWorkflowResponse;
import com.se1853_jv.readingservice.exception.ResourceNotFoundException;
import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.model.ReadingWorkflowId;
import com.se1853_jv.readingservice.repository.ReadingWorkflowRepository;
import com.se1853_jv.readingservice.service.ReadingWorkflowService;
import com.se1853_jv.readingservice.util.IdEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional

public class ReadingWorkflowServiceImpl implements ReadingWorkflowService {

    private final ReadingWorkflowRepository readingWorkflowRepository;

    @Override
    public ReadingWorkflowResponse createWorkflow(ReadingWorkflowCreateRequest request) {
        // Check if workflow already exists
        if (readingWorkflowRepository.existsById_CollectionIdAndId_PaperIdAndId_Usersid(
                request.getCollectionId(), request.getPaperId(), request.getUsersid())) {
            // Return existing workflow
            ReadingWorkflow existing = readingWorkflowRepository
                    .findById_CollectionIdAndId_PaperIdAndId_Usersid(
                            request.getCollectionId(), request.getPaperId(), request.getUsersid())
                    .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
            return toResponse(existing);
        }

        // Create new workflow with default values
        ReadingWorkflowId id = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUsersid()
        );

        ReadingWorkflow workflow = ReadingWorkflow.builder()
                .id(id)
                .status("unread")
                .lastPage(0)
                .progress(0)
                .build();

        ReadingWorkflow saved = readingWorkflowRepository.save(workflow);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingWorkflowResponse> getWorkflowsByUser(String usersid, String status) {
        List<ReadingWorkflow> workflows;
        if (status != null && !status.isEmpty()) {
            workflows = readingWorkflowRepository.findByUsersidAndStatus(usersid, status);
        } else {
            workflows = readingWorkflowRepository.findById_Usersid(usersid);
        }
        
        return workflows.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateProgress(ReadingWorkflowProgressRequest request) {
        ReadingWorkflowId id = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUsersid()
        );

        ReadingWorkflow workflow = readingWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading workflow not found"));

        workflow.setLastPage(request.getLastPage());
        workflow.setProgress(request.getProgress());
        
        // Auto-update status if progress >= 100
        if (workflow.getProgress() >= 100 && !"finished".equals(workflow.getStatus())) {
            workflow.setStatus("finished");
        }

        readingWorkflowRepository.save(workflow);
    }

    @Override
    public void updateStatus(ReadingWorkflowStatusRequest request) {
        ReadingWorkflowId id = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUsersid()
        );

        ReadingWorkflow workflow = readingWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading workflow not found"));

        workflow.setStatus(request.getStatus());
        readingWorkflowRepository.save(workflow);
    }

    @Override
    public void deleteWorkflow(ReadingWorkflowDeleteRequest request) {
        ReadingWorkflowId id = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUsersid()
        );

        ReadingWorkflow workflow = readingWorkflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading workflow not found"));

        // Clear notes and highlights relationships (Many-to-Many handles this automatically)
        workflow.getNotes().clear();
        workflow.getHighlights().clear();
        
        // Delete the workflow (cascade will handle junction table cleanup)
        readingWorkflowRepository.deleteById(id);
    }

    private ReadingWorkflowResponse toResponse(ReadingWorkflow workflow) {
        return ReadingWorkflowResponse.builder()
                .collectionId(IdEncoder.encode(workflow.getId().getCollectionId()))
                .paperId(IdEncoder.encode(workflow.getId().getPaperId()))
                .usersid(IdEncoder.encode(workflow.getId().getUsersid()))
                .status(workflow.getStatus())
                .lastPage(workflow.getLastPage())
                .progress(workflow.getProgress())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }
}
