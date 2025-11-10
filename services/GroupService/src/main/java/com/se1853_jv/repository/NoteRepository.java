package com.se1853_jv.repository;

import com.se1853_jv.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {
    
    /**
     * Find notes by paper ID
     */
    List<Note> findByPaperId(String paperId);
    
    /**
     * Find notes by paper ID and collection ID
     */
    List<Note> findByPaperIdAndCollectionId(String paperId, String collectionId);
    
    /**
     * Find notes by user ID, paper ID, and collection ID
     */
    List<Note> findByUserIdAndPaperIdAndCollectionId(String userId, String paperId, String collectionId);
    
    /**
     * Find notes by paper ID and user ID (for personal collections)
     */
    List<Note> findByPaperIdAndUserId(String paperId, String userId);
}

