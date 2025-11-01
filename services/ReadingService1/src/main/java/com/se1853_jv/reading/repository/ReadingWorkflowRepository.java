package com.se1853_jv.reading.repository;

import com.se1853_jv.reading.model.ReadingWorkflow;
import com.se1853_jv.reading.model.ReadingWorkflowId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingWorkflowRepository extends JpaRepository<ReadingWorkflow, ReadingWorkflowId> {
    List<ReadingWorkflow> findByIdUserId(String userId);
}

