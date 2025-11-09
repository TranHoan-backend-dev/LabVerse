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
    
    Optional<ReadingWorkflow> findById_CollectionIdAndId_PaperIdAndId_Usersid(
            String collectionId, String paperId, String usersid);
    
    List<ReadingWorkflow> findById_Usersid(String usersid);
    
    @Query("SELECT r FROM ReadingWorkflow r WHERE r.id.usersid = :usersid AND " +
           "(:status IS NULL OR r.status = :status)")
    List<ReadingWorkflow> findByUsersidAndStatus(
            @Param("usersid") String usersid, 
            @Param("status") String status);
    
    boolean existsById_CollectionIdAndId_PaperIdAndId_Usersid(
            String collectionId, String paperId, String usersid);
    
    // Collection-based queries for progress tracking
    List<ReadingWorkflow> findById_CollectionId(String collectionId);
    
    @Query("SELECT r FROM ReadingWorkflow r WHERE r.id.collectionId = :collectionId AND " +
           "(:status IS NULL OR r.status = :status)")
    List<ReadingWorkflow> findByCollectionIdAndStatus(
            @Param("collectionId") String collectionId,
            @Param("status") String status);
    
    @Query("SELECT r FROM ReadingWorkflow r WHERE r.id.collectionId = :collectionId AND r.id.paperId = :paperId")
    List<ReadingWorkflow> findByCollectionIdAndPaperId(
            @Param("collectionId") String collectionId,
            @Param("paperId") String paperId);
    
    @Query("SELECT r FROM ReadingWorkflow r WHERE r.id.collectionId = :collectionId AND r.id.usersid = :usersid")
    List<ReadingWorkflow> findByCollectionIdAndUsersid(
            @Param("collectionId") String collectionId,
            @Param("usersid") String usersid);
}

