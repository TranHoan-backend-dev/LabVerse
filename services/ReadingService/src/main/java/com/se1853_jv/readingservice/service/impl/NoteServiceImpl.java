package com.se1853_jv.readingservice.service.impl;

import com.se1853_jv.readingservice.dto.request.NoteRequest;
import com.se1853_jv.readingservice.dto.request.NoteUpdateRequest;
import com.se1853_jv.readingservice.dto.response.NoteResponse;
import com.se1853_jv.readingservice.exception.ResourceNotFoundException;
import com.se1853_jv.readingservice.model.Note;
import com.se1853_jv.readingservice.model.ReadingWorkflow;
import com.se1853_jv.readingservice.model.ReadingWorkflowId;
import com.se1853_jv.readingservice.repository.NoteRepository;
import com.se1853_jv.readingservice.repository.ReadingWorkflowRepository;
import com.se1853_jv.readingservice.service.NoteService;
import com.se1853_jv.readingservice.util.IdEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final ReadingWorkflowRepository readingWorkflowRepository;

    @Override
    public NoteResponse addNote(NoteRequest request) {
        // Ensure workflow exists
        ReadingWorkflowId workflowId = new ReadingWorkflowId(
                request.getCollectionId(),
                request.getPaperId(),
                request.getUsersid()
        );
        
        ReadingWorkflow workflow = readingWorkflowRepository.findById(workflowId)
                .orElseGet(() -> {
                    // Create workflow if not exists
                    ReadingWorkflow newWorkflow = ReadingWorkflow.builder()
                            .id(workflowId)
                            .status("reading")
                            .lastPage(request.getPageNumber())
                            .progress(0)
                            .build();
                    return readingWorkflowRepository.save(newWorkflow);
                });

        // Create note with UUID
        String noteId = java.util.UUID.randomUUID().toString();
        Note note = Note.builder()
                .id(noteId)
                .content(request.getContent())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .build();

        Note savedNote = noteRepository.save(note);

        // Add note to workflow's notes collection (Many-to-Many)
        if (workflow.getNotes() == null) {
            workflow.setNotes(new java.util.ArrayList<>());
        }
        workflow.getNotes().add(savedNote);
        readingWorkflowRepository.save(workflow);

        return toResponse(savedNote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoteResponse> getNotes(String collectionId, String paperId, String usersid) {
        List<Note> notes = noteRepository.findByCollectionIdAndPaperIdAndUsersid(collectionId, paperId, usersid);
        return notes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponse updateNote(String noteId, NoteUpdateRequest request) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        note.setContent(request.getContent());
        Note updated = noteRepository.save(note);
        return toResponse(updated);
    }

    @Override
    public void deleteNote(String noteId) {
        // Check if note exists
        if (!noteRepository.existsById(noteId)) {
            throw new ResourceNotFoundException("Note not found");
        }
        
        // Remove note from all workflows
        List<ReadingWorkflow> workflows = readingWorkflowRepository.findAll();
        for (ReadingWorkflow workflow : workflows) {
            if (workflow.getNotes() != null) {
                workflow.getNotes().removeIf(n -> n.getId().equals(noteId));
                readingWorkflowRepository.save(workflow);
            }
        }
        
        // Delete note
        noteRepository.deleteById(noteId);
    }

    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(IdEncoder.encode(note.getId()))
                .content(note.getContent())
                .coordinationX(note.getCoordinationX())
                .coordinationY(note.getCoordinationY())
                .pageNumber(note.getPageNumber())
                .createdAt(note.getCreatedAt())
                .build();
    }
}

