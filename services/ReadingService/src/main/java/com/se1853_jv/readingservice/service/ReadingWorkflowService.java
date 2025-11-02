package com.se1853_jv.readingservice.service;

import com.se1853_jv.readingservice.dto.request.ReadingWorkflowCreateRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowDeleteRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowProgressRequest;
import com.se1853_jv.readingservice.dto.request.ReadingWorkflowStatusRequest;
import com.se1853_jv.readingservice.dto.response.ReadingWorkflowResponse;

import java.util.List;

public interface ReadingWorkflowService {

    ReadingWorkflowResponse createWorkflow(ReadingWorkflowCreateRequest request);

    List<ReadingWorkflowResponse> getWorkflowsByUser(String usersid, String status);

    void updateProgress(ReadingWorkflowProgressRequest request);

    void updateStatus(ReadingWorkflowStatusRequest request);

    void deleteWorkflow(ReadingWorkflowDeleteRequest request);
}
