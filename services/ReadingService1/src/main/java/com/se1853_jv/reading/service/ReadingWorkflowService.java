package com.se1853_jv.reading.service;

import com.se1853_jv.reading.dto.request.ReadingWorkflowCreateRequest;
import com.se1853_jv.reading.dto.request.ReadingWorkflowUpdateRequest;
import com.se1853_jv.reading.dto.response.ReadingWorkflowResponse;

import java.util.List;

public interface ReadingWorkflowService {

    ReadingWorkflowResponse createWorkflow(ReadingWorkflowCreateRequest request);

    ReadingWorkflowResponse updateWorkflow(ReadingWorkflowUpdateRequest request);

    ReadingWorkflowResponse getWorkflow(String collectionId, String paperId, String userId);

    List<ReadingWorkflowResponse> getWorkflowsByUser(String userId);
}

