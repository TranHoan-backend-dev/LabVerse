package com.se1853_jv.service.impl;

import com.se1853_jv.dto.request.CreateHighlightRequest;
import com.se1853_jv.dto.request.CreateNoteRequest;
import com.se1853_jv.dto.response.ExportAnnotationsResponse;
import com.se1853_jv.dto.response.HighlightResponse;
import com.se1853_jv.dto.response.NoteResponse;
import com.se1853_jv.model.Highlight;
import com.se1853_jv.model.Note;
import com.se1853_jv.repository.HighlightRepository;
import com.se1853_jv.repository.NoteRepository;
import com.se1853_jv.service.AnnotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnotationServiceImpl implements AnnotationService {
    
    private final NoteRepository noteRepository;
    private final HighlightRepository highlightRepository;
    
    @Override
    public NoteResponse createNote(String userId, CreateNoteRequest request) {
        Note note = Note.builder()
                .id(UUID.randomUUID().toString())
                .paperId(request.getPaperId())
                .collectionId(request.getCollectionId())
                .userId(userId)
                .content(request.getContent())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .build();
        
        Note savedNote = noteRepository.save(note);
        return mapToNoteResponse(savedNote);
    }
    
    @Override
    public HighlightResponse createHighlight(String userId, CreateHighlightRequest request) {
        Highlight highlight = Highlight.builder()
                .id(UUID.randomUUID().toString())
                .paperId(request.getPaperId())
                .collectionId(request.getCollectionId())
                .userId(userId)
                .color(request.getColor())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .build();
        
        Highlight savedHighlight = highlightRepository.save(highlight);
        return mapToHighlightResponse(savedHighlight);
    }
    
    @Override
    public List<NoteResponse> getNotes(String paperId, String collectionId, String userId) {
        List<Note> notes;
        if (userId != null && collectionId != null) {
            notes = noteRepository.findByUserIdAndPaperIdAndCollectionId(userId, paperId, collectionId);
        } else if (collectionId != null) {
            notes = noteRepository.findByPaperIdAndCollectionId(paperId, collectionId);
        } else if (userId != null) {
            notes = noteRepository.findByPaperIdAndUserId(paperId, userId);
        } else {
            notes = noteRepository.findByPaperId(paperId);
        }
        
        return notes.stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HighlightResponse> getHighlights(String paperId, String collectionId, String userId) {
        List<Highlight> highlights;
        if (userId != null && collectionId != null) {
            highlights = highlightRepository.findByUserIdAndPaperIdAndCollectionId(userId, paperId, collectionId);
        } else if (collectionId != null) {
            highlights = highlightRepository.findByPaperIdAndCollectionId(paperId, collectionId);
        } else if (userId != null) {
            highlights = highlightRepository.findByPaperIdAndUserId(paperId, userId);
        } else {
            highlights = highlightRepository.findByPaperId(paperId);
        }
        
        return highlights.stream()
                .map(this::mapToHighlightResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteNote(String userId, String noteId, String paperId, String collectionId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        if (!note.getUserId().equals(userId) || !note.getPaperId().equals(paperId) || !note.getCollectionId().equals(collectionId)) {
            throw new IllegalArgumentException("Note does not belong to user or collection");
        }
        
        noteRepository.deleteById(noteId);
    }
    
    @Override
    public void deleteHighlight(String userId, String highlightId, String paperId, String collectionId) {
        Highlight highlight = highlightRepository.findById(highlightId)
                .orElseThrow(() -> new IllegalArgumentException("Highlight not found"));
        
        if (!highlight.getUserId().equals(userId) || !highlight.getPaperId().equals(paperId) || !highlight.getCollectionId().equals(collectionId)) {
            throw new IllegalArgumentException("Highlight does not belong to user or collection");
        }
        
        highlightRepository.deleteById(highlightId);
    }
    
    private NoteResponse mapToNoteResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .content(note.getContent())
                .coordinationX(note.getCoordinationX().longValue())
                .coordinationY(note.getCoordinationY().longValue())
                .pageNumber(note.getPageNumber())
                .paperId(UUID.fromString(note.getPaperId()))
                .collectionId(UUID.fromString(note.getCollectionId()))
                .userId(UUID.fromString(note.getUserId()))
                .build();
    }
    
    private HighlightResponse mapToHighlightResponse(Highlight highlight) {
        return HighlightResponse.builder()
                .id(highlight.getId())
                .color(highlight.getColor())
                .coordinationX(highlight.getCoordinationX().longValue())
                .coordinationY(highlight.getCoordinationY().longValue())
                .pageNumber(highlight.getPageNumber())
                .paperId(UUID.fromString(highlight.getPaperId()))
                .collectionId(UUID.fromString(highlight.getCollectionId()))
                .userId(UUID.fromString(highlight.getUserId()))
                .build();
    }
    
    @Override
    public ExportAnnotationsResponse exportAnnotations(String paperId, String collectionId, String userId) {
        // Get all notes and highlights for this paper and collection
        List<Note> notes = noteRepository.findByPaperIdAndCollectionId(paperId, collectionId);
        List<Highlight> highlights = highlightRepository.findByPaperIdAndCollectionId(paperId, collectionId);
        
        List<NoteResponse> noteResponses = notes.stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
        
        List<HighlightResponse> highlightResponses = highlights.stream()
                .map(this::mapToHighlightResponse)
                .collect(Collectors.toList());
        
        return ExportAnnotationsResponse.builder()
                .paperId(paperId)
                .collectionId(collectionId)
                .exportedBy(userId)
                .exportedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .notes(noteResponses)
                .highlights(highlightResponses)
                .build();
    }
}

