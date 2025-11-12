package com.se1853_jv.service.impl;

import com.se1853_jv.dto.request.CreateHighlightRequest;
import com.se1853_jv.dto.request.CreateNoteRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.se1853_jv.dto.response.AnnotationExportSummaryResponse;
import com.se1853_jv.dto.response.ExportAnnotationsResponse;
import com.se1853_jv.dto.response.HighlightResponse;
import com.se1853_jv.dto.response.NoteResponse;
import com.se1853_jv.model.AnnotationExport;
import com.se1853_jv.model.Highlight;
import com.se1853_jv.model.Note;
import com.se1853_jv.repository.AnnotationExportRepository;
import com.se1853_jv.repository.HighlightRepository;
import com.se1853_jv.repository.NoteRepository;
import com.se1853_jv.service.AnnotationService;
import com.se1853_jv.util.IdEncoder;
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
    private final AnnotationExportRepository annotationExportRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public NoteResponse createNote(String userId, CreateNoteRequest request) {
        // Decode encoded IDs from Android (paperId and collectionId from request)
        // userId from JWT token is already a valid UUID, no need to decode
        String decodedPaperId = IdEncoder.decode(request.getPaperId());
        String decodedCollectionId = IdEncoder.decode(request.getCollectionId());
        
        // Use decoded IDs if they are valid UUIDs, otherwise use original
        String paperId = decodedPaperId != null && decodedPaperId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$") 
                ? decodedPaperId : request.getPaperId();
        String collectionId = decodedCollectionId != null && decodedCollectionId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
                ? decodedCollectionId : request.getCollectionId();
        String finalUserId = userId; // userId from JWT is already UUID, no decode needed
        
        log.info("Creating note for user={}, paper={}, collection={}", finalUserId, paperId, collectionId);
        
        Note note = Note.builder()
                .id(UUID.randomUUID().toString())
                .paperId(paperId)
                .collectionId(collectionId)
                .userId(finalUserId)
                .content(request.getContent())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .createdAt(LocalDateTime.now())
                .build();
        
        Note savedNote = noteRepository.save(note);
        
        log.info("Note created successfully: {}", savedNote.getId());
        return mapToNoteResponse(savedNote);
    }
    
    @Override
    public HighlightResponse createHighlight(String userId, CreateHighlightRequest request) {
        // Decode encoded IDs from Android (paperId and collectionId from request)
        // userId from JWT token is already a valid UUID, no need to decode
        String decodedPaperId = IdEncoder.decode(request.getPaperId());
        String decodedCollectionId = IdEncoder.decode(request.getCollectionId());
        
        // Use decoded IDs if they are valid UUIDs, otherwise use original
        String paperId = decodedPaperId != null && decodedPaperId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
                ? decodedPaperId : request.getPaperId();
        String collectionId = decodedCollectionId != null && decodedCollectionId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
                ? decodedCollectionId : request.getCollectionId();
        String finalUserId = userId; // userId from JWT is already UUID, no decode needed
        
        log.info("Creating highlight for user={}, paper={}, collection={}", finalUserId, paperId, collectionId);
        
        Highlight highlight = Highlight.builder()
                .id(UUID.randomUUID().toString())
                .paperId(paperId)
                .collectionId(collectionId)
                .userId(finalUserId)
                .color(request.getColor())
                .coordinationX(request.getCoordinationX())
                .coordinationY(request.getCoordinationY())
                .pageNumber(request.getPageNumber())
                .createdAt(LocalDateTime.now())
                .build();
        
        Highlight savedHighlight = highlightRepository.save(highlight);
        
        log.info("Highlight created successfully: {}", savedHighlight.getId());
        return mapToHighlightResponse(savedHighlight);
    }
    
    @Override
    public List<NoteResponse> getNotes(String paperId, String collectionId, String userId) {
        // Decode encoded IDs from Android
        String decodedPaperId = paperId != null ? IdEncoder.decode(paperId) : null;
        String decodedCollectionId = collectionId != null ? IdEncoder.decode(collectionId) : null;
        String decodedUserId = userId != null ? IdEncoder.decode(userId) : null;
        
        // Use decoded IDs, fallback to original if decode fails
        String finalPaperId = decodedPaperId != null ? decodedPaperId : paperId;
        String finalCollectionId = decodedCollectionId != null ? decodedCollectionId : collectionId;
        String finalUserId = decodedUserId != null ? decodedUserId : userId;
        
        log.info("Getting notes for paper={}, collection={}, user={}", finalPaperId, finalCollectionId, finalUserId);
        
        List<Note> notes;
        if (finalUserId != null && finalCollectionId != null) {
            notes = noteRepository.findByUserIdAndPaperIdAndCollectionId(finalUserId, finalPaperId, finalCollectionId);
        } else if (finalCollectionId != null) {
            notes = noteRepository.findByPaperIdAndCollectionId(finalPaperId, finalCollectionId);
        } else if (finalUserId != null) {
            notes = noteRepository.findByPaperIdAndUserId(finalPaperId, finalUserId);
        } else {
            notes = noteRepository.findByPaperId(finalPaperId);
        }
        
        return notes.stream()
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<HighlightResponse> getHighlights(String paperId, String collectionId, String userId) {
        // Decode encoded IDs from Android
        String decodedPaperId = paperId != null ? IdEncoder.decode(paperId) : null;
        String decodedCollectionId = collectionId != null ? IdEncoder.decode(collectionId) : null;
        String decodedUserId = userId != null ? IdEncoder.decode(userId) : null;
        
        // Use decoded IDs, fallback to original if decode fails
        String finalPaperId = decodedPaperId != null ? decodedPaperId : paperId;
        String finalCollectionId = decodedCollectionId != null ? decodedCollectionId : collectionId;
        String finalUserId = decodedUserId != null ? decodedUserId : userId;
        
        log.info("Getting highlights for paper={}, collection={}, user={}", finalPaperId, finalCollectionId, finalUserId);
        
        List<Highlight> highlights;
        if (finalUserId != null && finalCollectionId != null) {
            highlights = highlightRepository.findByUserIdAndPaperIdAndCollectionId(finalUserId, finalPaperId, finalCollectionId);
        } else if (finalCollectionId != null) {
            highlights = highlightRepository.findByPaperIdAndCollectionId(finalPaperId, finalCollectionId);
        } else if (finalUserId != null) {
            highlights = highlightRepository.findByPaperIdAndUserId(finalPaperId, finalUserId);
        } else {
            highlights = highlightRepository.findByPaperId(finalPaperId);
        }
        
        return highlights.stream()
                .map(this::mapToHighlightResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteNote(String userId, String noteId, String paperId, String collectionId) {
        log.info("Deleting note={} for user={}", noteId, userId);
        
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        
        if (!note.getUserId().equals(userId) || !note.getPaperId().equals(paperId) || !note.getCollectionId().equals(collectionId)) {
            throw new IllegalArgumentException("Note does not belong to user or collection");
        }
        
        noteRepository.deleteById(noteId);
        log.info("Note deleted successfully: {}", noteId);
    }
    
    @Override
    public void deleteHighlight(String userId, String highlightId, String paperId, String collectionId) {
        log.info("Deleting highlight={} for user={}", highlightId, userId);
        
        Highlight highlight = highlightRepository.findById(highlightId)
                .orElseThrow(() -> new IllegalArgumentException("Highlight not found"));
        
        if (!highlight.getUserId().equals(userId) || !highlight.getPaperId().equals(paperId) || !highlight.getCollectionId().equals(collectionId)) {
            throw new IllegalArgumentException("Highlight does not belong to user or collection");
        }
        
        highlightRepository.deleteById(highlightId);
        log.info("Highlight deleted successfully: {}", highlightId);
    }
    
    private NoteResponse mapToNoteResponse(Note note) {
        try {
            return NoteResponse.builder()
                    .id(note.getId())
                    .content(note.getContent())
                    .coordinationX(note.getCoordinationX().longValue())
                    .coordinationY(note.getCoordinationY().longValue())
                    .pageNumber(note.getPageNumber())
                    .paperId(parseUUID(note.getPaperId()))
                    .collectionId(parseUUID(note.getCollectionId()))
                    .userId(parseUUID(note.getUserId()))
                    .build();
        } catch (Exception e) {
            log.error("Error mapping note to response: noteId={}, error={}", note.getId(), e.getMessage());
            throw new IllegalStateException("Failed to map note to response: " + e.getMessage(), e);
        }
    }
    
    private HighlightResponse mapToHighlightResponse(Highlight highlight) {
        try {
            return HighlightResponse.builder()
                    .id(highlight.getId())
                    .color(highlight.getColor())
                    .coordinationX(highlight.getCoordinationX().longValue())
                    .coordinationY(highlight.getCoordinationY().longValue())
                    .pageNumber(highlight.getPageNumber())
                    .paperId(parseUUID(highlight.getPaperId()))
                    .collectionId(parseUUID(highlight.getCollectionId()))
                    .userId(parseUUID(highlight.getUserId()))
                    .build();
        } catch (Exception e) {
            log.error("Error mapping highlight to response: highlightId={}, error={}", highlight.getId(), e.getMessage());
            throw new IllegalStateException("Failed to map highlight to response: " + e.getMessage(), e);
        }
    }
    
    /**
     * Safely parse a string to UUID, handling both valid UUIDs and potentially encoded strings
     */
    private UUID parseUUID(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        
        // First try to parse directly as UUID
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // If direct parse fails, try to decode first (in case it's Base64 encoded)
            String decoded = IdEncoder.decode(id);
            try {
                return UUID.fromString(decoded);
            } catch (IllegalArgumentException e2) {
                log.error("Failed to parse UUID: original={}, decoded={}", id, decoded);
                throw new IllegalArgumentException("Invalid UUID string: " + id, e2);
            }
        }
    }
    
    @Override
    public ExportAnnotationsResponse exportAnnotations(String paperId, String collectionId, String userId) {
        // userId from JWT token is already a valid UUID, no need to decode
        // Only decode paperId and collectionId from query parameters
        String decodedPaperId = IdEncoder.decode(paperId);
        String decodedCollectionId = IdEncoder.decode(collectionId);
        
        // Check if decode succeeded (result is a valid UUID format)
        // IdEncoder.decode() returns original string if decode fails, so check if it's a valid UUID
        boolean isPaperIdUuid = decodedPaperId != null && decodedPaperId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        boolean isCollectionIdUuid = decodedCollectionId != null && decodedCollectionId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        
        // Use decoded IDs if they are valid UUIDs, otherwise use original
        String finalPaperId = isPaperIdUuid ? decodedPaperId : paperId;
        String finalCollectionId = isCollectionIdUuid ? decodedCollectionId : collectionId;
        String finalUserId = userId; // userId from JWT is already UUID, no decode needed
        
        log.info("Exporting annotations: original paperId={}, decoded={}, final={}", paperId, decodedPaperId, finalPaperId);
        log.info("Exporting annotations: original collectionId={}, decoded={}, final={}", collectionId, decodedCollectionId, finalCollectionId);
        log.info("Exporting annotations for paper={}, collection={}, user={}", finalPaperId, finalCollectionId, finalUserId);
        
        // Get all notes and highlights for this paper and collection
        List<Note> notes = noteRepository.findByPaperIdAndCollectionId(finalPaperId, finalCollectionId);
        List<Highlight> highlights = highlightRepository.findByPaperIdAndCollectionId(finalPaperId, finalCollectionId);
        
        // Filter out notes with corrupted data and map to responses
        List<NoteResponse> noteResponses = notes.stream()
                .filter(note -> {
                    try {
                        // Try to validate UUIDs before mapping
                        parseUUID(note.getPaperId());
                        parseUUID(note.getCollectionId());
                        parseUUID(note.getUserId());
                        return true;
                    } catch (Exception e) {
                        log.warn("Skipping note with corrupted data: noteId={}, error={}", note.getId(), e.getMessage());
                        return false;
                    }
                })
                .map(this::mapToNoteResponse)
                .collect(Collectors.toList());
        
        // Filter out highlights with corrupted data and map to responses
        List<HighlightResponse> highlightResponses = highlights.stream()
                .filter(highlight -> {
                    try {
                        // Try to validate UUIDs before mapping
                        parseUUID(highlight.getPaperId());
                        parseUUID(highlight.getCollectionId());
                        parseUUID(highlight.getUserId());
                        return true;
                    } catch (Exception e) {
                        log.warn("Skipping highlight with corrupted data: highlightId={}, error={}", highlight.getId(), e.getMessage());
                        return false;
                    }
                })
                .map(this::mapToHighlightResponse)
                .collect(Collectors.toList());
        
        ExportAnnotationsResponse exportResponse = ExportAnnotationsResponse.builder()
                .paperId(finalPaperId)
                .collectionId(finalCollectionId)
                .exportedBy(finalUserId)
                .exportedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .notes(noteResponses)
                .highlights(highlightResponses)
                .build();

        persistExportSnapshot(exportResponse);

        return exportResponse;
    }

    @Override
    public List<AnnotationExportSummaryResponse> listExports(String collectionId, String paperId) {
        // Decode encoded IDs from Android
        String decodedCollectionId = collectionId != null ? IdEncoder.decode(collectionId) : null;
        String decodedPaperId = paperId != null ? IdEncoder.decode(paperId) : null;
        
        // Use decoded IDs, fallback to original if decode fails
        String finalCollectionId = decodedCollectionId != null ? decodedCollectionId : collectionId;
        String finalPaperId = decodedPaperId != null ? decodedPaperId : paperId;
        
        log.info("Listing exports for collection={}, paper={}", finalCollectionId, finalPaperId);

        List<AnnotationExport> exports = finalPaperId != null && !finalPaperId.isBlank()
                ? annotationExportRepository.findByPaperIdAndCollectionIdOrderByExportedAtDesc(finalPaperId, finalCollectionId)
                : annotationExportRepository.findByCollectionIdOrderByExportedAtDesc(finalCollectionId);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        return exports.stream()
                .map(export -> AnnotationExportSummaryResponse.builder()
                        .exportId(export.getId())
                        .paperId(export.getPaperId())
                        .collectionId(export.getCollectionId())
                        .exportedBy(export.getExportedBy())
                        .exportedAt(export.getExportedAt().format(formatter))
                        .totalNotes(export.getTotalNotes())
                        .totalHighlights(export.getTotalHighlights())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ExportAnnotationsResponse getExportDetail(String exportId) {
        log.info("Getting export detail for id={}", exportId);

        AnnotationExport export = annotationExportRepository.findById(exportId)
                .orElseThrow(() -> new IllegalArgumentException("Annotation export not found"));

        try {
            ExportAnnotationsResponse response = objectMapper.readValue(export.getPayload(), ExportAnnotationsResponse.class);
            response.setExportedAt(export.getExportedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.setExportedBy(export.getExportedBy());
            return response;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse export payload", e);
        }
    }

    private void persistExportSnapshot(ExportAnnotationsResponse exportResponse) {
        try {
            LocalDateTime exportedAt = LocalDateTime.parse(exportResponse.getExportedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Decode IDs to ensure they are in UUID format before saving to database
            String paperId = exportResponse.getPaperId();
            String collectionId = exportResponse.getCollectionId();
            String exportedBy = exportResponse.getExportedBy();
            
            // Try to decode paperId and collectionId (in case they're still encoded)
            // exportedBy is already a valid UUID from JWT token, no need to decode
            String decodedPaperId = IdEncoder.decode(paperId);
            String decodedCollectionId = IdEncoder.decode(collectionId);
            
            // Check if decode succeeded (result is a valid UUID format)
            boolean isPaperIdUuid = decodedPaperId != null && decodedPaperId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            boolean isCollectionIdUuid = decodedCollectionId != null && decodedCollectionId.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            
            // Use decoded IDs if they are valid UUIDs, otherwise use original
            String finalPaperId = isPaperIdUuid ? decodedPaperId : paperId;
            String finalCollectionId = isCollectionIdUuid ? decodedCollectionId : collectionId;
            String finalExportedBy = exportedBy; // Already UUID from JWT, no decode needed
            
            log.info("Persisting export snapshot: original collectionId={}, decoded={}, final={}", 
                    collectionId, decodedCollectionId, finalCollectionId);
            log.debug("Persisting export snapshot: paperId={}, collectionId={}, exportedBy={}", 
                    finalPaperId, finalCollectionId, finalExportedBy);

            String payload = objectMapper.writeValueAsString(exportResponse);

            AnnotationExport export = AnnotationExport.builder()
                    .paperId(finalPaperId)
                    .collectionId(finalCollectionId)
                    .exportedBy(finalExportedBy)
                    .payload(payload)
                    .exportedAt(exportedAt)
                    .totalNotes(exportResponse.getNotes() != null ? exportResponse.getNotes().size() : 0)
                    .totalHighlights(exportResponse.getHighlights() != null ? exportResponse.getHighlights().size() : 0)
                    .build();

            annotationExportRepository.save(export);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize export payload", e);
            throw new IllegalStateException("Unable to serialize export data", e);
        }
    }
}

