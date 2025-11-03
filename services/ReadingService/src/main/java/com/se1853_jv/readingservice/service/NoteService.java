package com.se1853_jv.readingservice.service;

import com.se1853_jv.readingservice.dto.request.NoteRequest;
import com.se1853_jv.readingservice.dto.request.NoteUpdateRequest;
import com.se1853_jv.readingservice.dto.response.NoteResponse;

import java.util.List;

public interface NoteService {

    NoteResponse addNote(NoteRequest request);

    List<NoteResponse> getNotes(String collectionId, String paperId, String usersid);

    NoteResponse updateNote(String noteId, NoteUpdateRequest request);

    void deleteNote(String noteId);
}

