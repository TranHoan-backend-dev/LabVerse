package com.se1853_jv.readingservice.service.impl;

import com.se1853_jv.readingservice.dto.request.NoteRequest;
import com.se1853_jv.readingservice.dto.request.NoteUpdateRequest;
import com.se1853_jv.readingservice.dto.response.NoteResponse;
import com.se1853_jv.readingservice.exception.ResourceNotFoundException;
import com.se1853_jv.readingservice.model.Note;
import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.model.ReadingWorkflowId;
import com.se1853_jv.readingservice.model.ReadingWorkflowNote;
import com.se1853_jv.readingservice.model.ReadingWorkflowNoteId;
import com.se1853_jv.readingservice.repository.NoteRepository;
import com.se1853_jv.readingservice.repository.ReadingWorkflowNoteRepository;
import com.se1853_jv.readingservice.repository.ReadingWorkflowRepository;
import com.se1853_jv.readingservice.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final ReadingWorkflowRepository readingWorkflowRepository;
    private final ReadingWorkflowNoteRepository readingWorkflowNoteRepository;

    @Override
    public NoteResponse addNote(NoteRequest request) {
        // Ensure workflow exists
        ReadingWorkflowId workflowId = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUserId()
        );
        
        if (!readingWorkflowRepository.existsById(workflowId)) {
            // Create workflow if not exists
            ReadingWorkflow workflow = ReadingWorkflow.builder()
                    .id(workflowId)
                    .status("reading")
                    .lastPage(request.getPageNumber())
                    .progress(0)
                    .build();
            readingWorkflowRepository.save(workflow);
        }

        // Create note (ID will be auto-generated)
        Note note = Note.builder()
                .content(request.getContent())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .build();

        Note savedNote = noteRepository.save(note);

        // Create mapping
        ReadingWorkflowNoteId mappingId = new ReadingWorkflowNoteId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUserId(),
                savedNote.getId().toString()
        );

        ReadingWorkflowNote mapping = ReadingWorkflowNote.builder()
                .id(mappingId)
                .build();

        readingWorkflowNoteRepository.save(mapping);

        return toResponse(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes(String collectionId, String paperId, String userId) {
        List<Note> notes = noteRepository.findByCollectionIdAndPaperIdAndUserId(collectionId, paperId, userId);
        return notes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponse updateNote(UUID noteId, NoteUpdateRequest request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        // Validate ownership - check if note is linked to a workflow
        // (In real scenario, you might want to check userId from workflow)
        note.setContent(request.getContent());
        
        Note updated = noteRepository.save(note);
        return toResponse(updated);
    }

    @Override
    public void deleteNote(UUID noteId) {
        // Delete mappings first
        readingWorkflowNoteRepository.deleteById_NoteId(noteId.toString());
        
        // Delete note
        noteRepository.deleteById(noteId);
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .content(note.getContent())
                .coordinationX(note.getCoordinationX())
                .coordinationY(note.getCoordinationY())
                .pageNumber(note.getPageNumber())
                .createdAt(note.getCreatedAt())
                .build();
    }
}

