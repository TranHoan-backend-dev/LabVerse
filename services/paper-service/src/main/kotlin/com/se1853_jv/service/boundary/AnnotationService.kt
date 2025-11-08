package com.se1853_jv.service.boundary

import com.se1853_jv.dto.request.CreateHighlightRequest
import com.se1853_jv.dto.request.CreateNoteRequest
import com.se1853_jv.dto.response.HighlightResponse
import com.se1853_jv.dto.response.NoteResponse

interface AnnotationService {
    /**
     * Create a new note for a user's reading workflow
     */
    fun createNote(userId: String, request: CreateNoteRequest): NoteResponse

    /**
     * Create a new highlight for a user's reading workflow
     */
    fun createHighlight(userId: String, request: CreateHighlightRequest): HighlightResponse

    /**
     * Get all notes for a paper (optionally filtered by collection)
     */
    fun getNotes(paperId: String, collectionId: String?, userId: String?): List<NoteResponse>

    /**
     * Get all highlights for a paper (optionally filtered by collection)
     */
    fun getHighlights(paperId: String, collectionId: String?, userId: String?): List<HighlightResponse>

    /**
     * Delete a note
     */
    fun deleteNote(userId: String, noteId: String, paperId: String, collectionId: String)

    /**
     * Delete a highlight
     */
    fun deleteHighlight(userId: String, highlightId: String, paperId: String, collectionId: String)
}







