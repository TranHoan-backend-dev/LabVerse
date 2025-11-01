package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.model.ReadingWorkflowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingWorkflowRepository extends JpaRepository<ReadingWorkflow, ReadingWorkflowId> {
    
    Optional<ReadingWorkflow> findById_CollectionIdAndId_PaperIdAndId_UserId(
            String collectionId, String paperId, String userId);
    
    List<ReadingWorkflow> findById_UserId(String userId);
    
    @Query("SELECT r FROM ReadingWorkflow r WHERE r.id.userId = :userId AND " +
           "(:status IS NULL OR r.status = :status)")
    List<ReadingWorkflow> findByUserIdAndStatus(
            @Param("userId") String userId, 
            @Param("status") String status);
    
    boolean existsById_CollectionIdAndId_PaperIdAndId_UserId(
            String collectionId, String paperId, String userId);
}

