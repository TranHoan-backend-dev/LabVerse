package com.se1853_jv.service;

import com.se1853_jv.dto.request.CreateHighlightRequest;
import com.se1853_jv.dto.request.CreateNoteRequest;
import com.se1853_jv.dto.response.ExportAnnotationsResponse;
import com.se1853_jv.dto.response.HighlightResponse;
import com.se1853_jv.dto.response.NoteResponse;

import java.util.List;

public interface AnnotationService {
    /**
     * Create a new note for a user's reading workflow
     */
    NoteResponse createNote(String userId, CreateNoteRequest request);
    
    /**
     * Create a new highlight for a user's reading workflow
     */
    HighlightResponse createHighlight(String userId, CreateHighlightRequest request);
    
    /**
     * Get all notes for a paper (optionally filtered by collection)
     */
    List<NoteResponse> getNotes(String paperId, String collectionId, String userId);
    
    /**
     * Get all highlights for a paper (optionally filtered by collection)
     */
    List<HighlightResponse> getHighlights(String paperId, String collectionId, String userId);
    
    /**
     * Delete a note
     */
    void deleteNote(String userId, String noteId, String paperId, String collectionId);
    
    /**
     * Delete a highlight
     */
    void deleteHighlight(String userId, String highlightId, String paperId, String collectionId);
    
    /**
     * Export all annotations (notes and highlights) for a paper in a collection
     * Used for sharing annotations between users
     */
    ExportAnnotationsResponse exportAnnotations(String paperId, String collectionId, String userId);
}

