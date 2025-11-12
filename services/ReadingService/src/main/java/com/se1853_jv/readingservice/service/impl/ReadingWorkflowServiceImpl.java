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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReadingWorkflowServiceImpl implements ReadingWorkflowService {

    private final ReadingWorkflowRepository readingWorkflowRepository;

    /**
     * Helper method to ensure IDs are decoded before use.
     * If ID is already decoded (UUID format), returns as-is.
     * If ID is encoded (base64), decodes it.
     */
    private String ensureDecodedId(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        // Check if it's a UUID format (decoded) - UUIDs have format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
        if (id.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return id; // Already decoded
        }
        // Try to decode - if it fails, assume it's already decoded
        try {
            return IdEncoder.decodeString(id);
        } catch (Exception e) {
            // If decode fails, assume it's already decoded or invalid
            log.warn("Failed to decode ID, using as-is: {}", id);
            return id;
        }
    }

    /**
     * Helper method to find workflow by ID, trying both decoded and encoded versions.
     * This handles the case where database might have encoded IDs from old data.
     * Automatically migrates workflow from encoded to decoded IDs when found.
     */
    private Optional<ReadingWorkflow> findWorkflowById(String collectionId, String paperId, String usersid) {
        // First try with decoded IDs (normal case)
        ReadingWorkflowId decodedId = new ReadingWorkflowId(collectionId, paperId, usersid);
        Optional<ReadingWorkflow> workflow = readingWorkflowRepository.findById(decodedId);
        
        if (workflow.isPresent()) {
            return workflow;
        }
        
        // If not found, try with encoded IDs (for old data in database)
        try {
            String encodedCollectionId = IdEncoder.encode(collectionId);
            String encodedPaperId = IdEncoder.encode(paperId);
            String encodedUsersid = IdEncoder.encode(usersid);
            ReadingWorkflowId encodedId = new ReadingWorkflowId(encodedCollectionId, encodedPaperId, encodedUsersid);
            workflow = readingWorkflowRepository.findById(encodedId);
            
            if (workflow.isPresent()) {
                // Migrate workflow to use decoded IDs
                ReadingWorkflow wf = workflow.get();
                
                // Force load relationships before migration (they are LAZY)
                var notes = new java.util.ArrayList<>(wf.getNotes());
                var highlights = new java.util.ArrayList<>(wf.getHighlights());
                
                // Get all data before deletion
                String status = wf.getStatus();
                Integer lastPage = wf.getLastPage();
                Integer progress = wf.getProgress();
                LocalDateTime createdAt = wf.getCreatedAt();
                LocalDateTime updatedAt = wf.getUpdatedAt();
                
                // Delete old workflow with encoded IDs (this will cascade delete junction table entries)
                readingWorkflowRepository.delete(wf);
                readingWorkflowRepository.flush(); // Ensure deletion is committed
                
                // Create new workflow with decoded IDs
                ReadingWorkflowId newId = new ReadingWorkflowId(collectionId, paperId, usersid);
                ReadingWorkflow migratedWorkflow = ReadingWorkflow.builder()
                        .id(newId)
                        .status(status)
                        .lastPage(lastPage)
                        .progress(progress)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .build();
                
                ReadingWorkflow saved = readingWorkflowRepository.save(migratedWorkflow);
                readingWorkflowRepository.flush(); // Ensure new workflow is persisted
                
                // Restore relationships (re-establish Many-to-Many mappings)
                if (!notes.isEmpty() || !highlights.isEmpty()) {
                    saved.getNotes().addAll(notes);
                    saved.getHighlights().addAll(highlights);
                    saved = readingWorkflowRepository.save(saved);
                }
                
                return Optional.of(saved);
            }
        } catch (Exception e) {
            log.debug("Failed to try encoded ID lookup: {}", e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public ReadingWorkflowResponse createWorkflow(ReadingWorkflowCreateRequest request) {
        // Ensure all IDs are decoded before use
        String collectionId = ensureDecodedId(request.getCollectionId());
        String paperId = ensureDecodedId(request.getPaperId());
        String usersid = ensureDecodedId(request.getUsersid());
        
        // Check if workflow already exists (try both decoded and encoded IDs)
        Optional<ReadingWorkflow> existing = findWorkflowById(collectionId, paperId, usersid);
        if (existing.isPresent()) {
            // Return existing workflow
            return toResponse(existing.get());
        }

        // Create new workflow with default values
        ReadingWorkflowId id = new ReadingWorkflowId(
                collectionId,
                paperId,
                usersid
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
        // Ensure ID is decoded before use
        String decodedUsersid = ensureDecodedId(usersid);
        
        List<ReadingWorkflow> workflows;
        if (status != null && !status.isEmpty()) {
            workflows = readingWorkflowRepository.findByUsersidAndStatus(decodedUsersid, status);
        } else {
            workflows = readingWorkflowRepository.findById_Usersid(decodedUsersid);
        }
        
        return workflows.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReadingWorkflowResponse> getWorkflowsByCollection(String collectionId, String status) {
        // Ensure ID is decoded before use
        String decodedCollectionId = ensureDecodedId(collectionId);
        
        List<ReadingWorkflow> workflows;
        if (status != null && !status.isEmpty()) {
            workflows = readingWorkflowRepository.findByCollectionIdAndStatus(decodedCollectionId, status);
        } else {
            workflows = readingWorkflowRepository.findById_CollectionId(decodedCollectionId);
        }
        
        return workflows.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateProgress(ReadingWorkflowProgressRequest request) {
        // Ensure all IDs are decoded before use
        String collectionId = ensureDecodedId(request.getCollectionId());
        String paperId = ensureDecodedId(request.getPaperId());
        String usersid = ensureDecodedId(request.getUsersid());
        
        ReadingWorkflowId id = new ReadingWorkflowId(
                collectionId,
                paperId,
                usersid
        );

        // Get existing workflow or create new one if it doesn't exist
        // Try to find with both decoded and encoded IDs (for backward compatibility)
        ReadingWorkflow workflow = findWorkflowById(collectionId, paperId, usersid)
                .orElseGet(() -> {
                    // Create new workflow if it doesn't exist
                    ReadingWorkflow newWorkflow = ReadingWorkflow.builder()
                            .id(id)
                            .status("reading")
                            .lastPage(0)
                            .progress(0)
                            .build();
                    return readingWorkflowRepository.save(newWorkflow);
                });

        workflow.setLastPage(request.getLastPage());
        workflow.setProgress(request.getProgress());
        
        // Auto-update status based on progress
        if (workflow.getProgress() >= 100) {
            workflow.setStatus("finished");
        } else if (workflow.getProgress() > 0 && !"finished".equals(workflow.getStatus())) {
            workflow.setStatus("reading");
        }

        readingWorkflowRepository.save(workflow);
    }

    @Override
    public void updateStatus(ReadingWorkflowStatusRequest request) {
        // Ensure all IDs are decoded before use
        String collectionId = ensureDecodedId(request.getCollectionId());
        String paperId = ensureDecodedId(request.getPaperId());
        String usersid = ensureDecodedId(request.getUsersid());
        
        ReadingWorkflowId id = new ReadingWorkflowId(
                collectionId,
                paperId,
                usersid
        );

        // Try to find with both decoded and encoded IDs (for backward compatibility)
        ReadingWorkflow workflow = findWorkflowById(collectionId, paperId, usersid)
                .orElseThrow(() -> new ResourceNotFoundException("Reading workflow not found"));

        workflow.setStatus(request.getStatus());
        readingWorkflowRepository.save(workflow);
    }

    @Override
    public void deleteWorkflow(ReadingWorkflowDeleteRequest request) {
        // Ensure all IDs are decoded before use
        String collectionId = ensureDecodedId(request.getCollectionId());
        String paperId = ensureDecodedId(request.getPaperId());
        String usersid = ensureDecodedId(request.getUsersid());

        // Try to find with both decoded and encoded IDs (for backward compatibility)
        ReadingWorkflow workflow = findWorkflowById(collectionId, paperId, usersid)
                .orElseThrow(() -> new ResourceNotFoundException("Reading workflow not found"));

        // Clear notes and highlights relationships (Many-to-Many handles this automatically)
        workflow.getNotes().clear();
        workflow.getHighlights().clear();
        
        // Delete the workflow using the actual workflow found (handles both decoded and encoded IDs)
        readingWorkflowRepository.delete(workflow);
    }

    private ReadingWorkflowResponse toResponse(ReadingWorkflow workflow) {
        // Ensure IDs are decoded before encoding for response
        // This handles the case where workflow might have encoded IDs from old data
        String collectionId = ensureDecodedId(workflow.getId().getCollectionId());
        String paperId = ensureDecodedId(workflow.getId().getPaperId());
        String usersid = ensureDecodedId(workflow.getId().getUsersid());
        
        return ReadingWorkflowResponse.builder()
                .collectionId(IdEncoder.encode(collectionId))
                .paperId(IdEncoder.encode(paperId))
                .usersid(IdEncoder.encode(usersid))
                .status(workflow.getStatus())
                .lastPage(workflow.getLastPage())
                .progress(workflow.getProgress())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .build();
    }
}
