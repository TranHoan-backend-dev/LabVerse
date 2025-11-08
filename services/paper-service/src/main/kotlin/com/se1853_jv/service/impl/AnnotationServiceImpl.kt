package com.se1853_jv.service.impl

import com.se1853_jv.dto.request.CreateHighlightRequest
import com.se1853_jv.dto.request.CreateNoteRequest
import com.se1853_jv.dto.response.HighlightResponse
import com.se1853_jv.dto.response.NoteResponse
import com.se1853_jv.model.Highlight
import com.se1853_jv.model.Note
import com.se1853_jv.repository.HighlightRepository
import com.se1853_jv.repository.NoteRepository
import com.se1853_jv.service.boundary.AnnotationService
import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class AnnotationServiceImpl(
    private val noteRepository: NoteRepository,
    private val highlightRepository: HighlightRepository
) : AnnotationService {

    override fun createNote(userId: String, request: CreateNoteRequest): NoteResponse {
        logger.info { "Creating note for user=$userId, paper=${request.paperId}, collection=${request.collectionId}" }

        // Create Note entity with all required fields (MongoDB - no junction tables needed)
        val note = Note(
            id = java.util.UUID.randomUUID().toString(),
            paperId = request.paperId,
            collectionId = request.collectionId,
            userId = userId,
            content = request.content,
            coordinationX = request.coordinationX,
            coordinationY = request.coordinationY,
            pageNumber = request.pageNumber
        )
        val savedNote = noteRepository.save(note)

        logger.info { "Note created successfully: ${savedNote.id}" }
        return mapToNoteResponse(savedNote)
    }

    override fun createHighlight(userId: String, request: CreateHighlightRequest): HighlightResponse {
        logger.info { "Creating highlight for user=$userId, paper=${request.paperId}, collection=${request.collectionId}" }

        // Create Highlight entity with all required fields (MongoDB - no junction tables needed)
        val highlight = Highlight(
            id = java.util.UUID.randomUUID().toString(),
            paperId = request.paperId,
            collectionId = request.collectionId,
            userId = userId,
            color = request.color,
            coordinationX = request.coordinationX,
            coordinationY = request.coordinationY,
            pageNumber = request.pageNumber
        )
        val savedHighlight = highlightRepository.save(highlight)

        logger.info { "Highlight created successfully: ${savedHighlight.id}" }
        return mapToHighlightResponse(savedHighlight)
    }

    override fun getNotes(paperId: String, collectionId: String?, userId: String?): List<NoteResponse> {
        logger.info { "Getting notes for paper=$paperId, collection=$collectionId, user=$userId" }

        val notes = when {
            userId != null && collectionId != null -> {
                noteRepository.findByUserIdAndPaperIdAndCollectionId(userId, paperId, collectionId)
            }
            collectionId != null -> {
                noteRepository.findByPaperIdAndCollectionId(paperId, collectionId)
            }
            userId != null -> {
                noteRepository.findByPaperIdAndUserId(paperId, userId)
            }
            else -> {
                noteRepository.findByPaperId(paperId)
            }
        }

        return notes.map { mapToNoteResponse(it) }
    }

    override fun getHighlights(paperId: String, collectionId: String?, userId: String?): List<HighlightResponse> {
        logger.info { "Getting highlights for paper=$paperId, collection=$collectionId, user=$userId" }

        val highlights = when {
            userId != null && collectionId != null -> {
                highlightRepository.findByUserIdAndPaperIdAndCollectionId(userId, paperId, collectionId)
            }
            collectionId != null -> {
                highlightRepository.findByPaperIdAndCollectionId(paperId, collectionId)
            }
            userId != null -> {
                highlightRepository.findByPaperIdAndUserId(paperId, userId)
            }
            else -> {
                highlightRepository.findByPaperId(paperId)
            }
        }

        return highlights.map { mapToHighlightResponse(it) }
    }

    override fun deleteNote(userId: String, noteId: String, paperId: String, collectionId: String) {
        logger.info { "Deleting note=$noteId for user=$userId" }

        // Verify note belongs to user
        val note: Note = noteRepository.findById(noteId)
            .orElseThrow { IllegalArgumentException("Note not found") }

        if (note.userId != userId || note.paperId != paperId || note.collectionId != collectionId) {
            throw IllegalArgumentException("Note does not belong to user or collection")
        }

        // Delete note (MongoDB - no junction table to delete)
        noteRepository.deleteById(noteId)
        logger.info { "Note deleted successfully: $noteId" }
    }

    override fun deleteHighlight(userId: String, highlightId: String, paperId: String, collectionId: String) {
        logger.info { "Deleting highlight=$highlightId for user=$userId" }

        // Verify highlight belongs to user
        val highlight: Highlight = highlightRepository.findById(highlightId)
            .orElseThrow { IllegalArgumentException("Note not found") }

        if (highlight.userId != userId || highlight.paperId != paperId || highlight.collectionId != collectionId) {
            throw IllegalArgumentException("Highlight does not belong to user or collection")
        }

        // Delete highlight (MongoDB - no junction table to delete)
        highlightRepository.deleteById(highlightId)
        logger.info { "Highlight deleted successfully: $highlightId" }
    }

    private fun mapToNoteResponse(note: Note): NoteResponse {
        return NoteResponse(
            id = note.id,
            content = note.content,
            coordinationX = note.coordinationX.toLong(),
            coordinationY = note.coordinationY.toLong(),
            pageNumber = note.pageNumber,
            paperId = java.util.UUID.fromString(note.paperId),
            collectionId = java.util.UUID.fromString(note.collectionId),
            userId = java.util.UUID.fromString(note.userId)
        )
    }

    private fun mapToHighlightResponse(highlight: Highlight): HighlightResponse {
        return HighlightResponse(
            id = highlight.id,
            color = highlight.color,
            coordinationX = highlight.coordinationX.toLong(),
            coordinationY = highlight.coordinationY.toLong(),
            pageNumber = highlight.pageNumber,
            paperId = java.util.UUID.fromString(highlight.paperId),
            collectionId = java.util.UUID.fromString(highlight.collectionId),
            userId = java.util.UUID.fromString(highlight.userId)
        )
    }
}







