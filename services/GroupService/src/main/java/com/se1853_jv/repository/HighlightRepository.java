package com.se1853_jv.repository;

import com.se1853_jv.model.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, String> {
    
    /**
     * Find highlights by paper ID
     */
    List<Highlight> findByPaperId(String paperId);
    
    /**
     * Find highlights by paper ID and collection ID
     */
    List<Highlight> findByPaperIdAndCollectionId(String paperId, String collectionId);
    
    /**
     * Find highlights by user ID, paper ID, and collection ID
     */
    List<Highlight> findByUserIdAndPaperIdAndCollectionId(String userId, String paperId, String collectionId);
    
    /**
     * Find highlights by paper ID and user ID (for personal collections)
     */
    List<Highlight> findByPaperIdAndUserId(String paperId, String userId);
}

