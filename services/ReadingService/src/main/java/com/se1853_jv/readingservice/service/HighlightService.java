package com.se1853_jv.readingservice.service;

import com.se1853_jv.readingservice.dto.request.HighlightRequest;
import com.se1853_jv.readingservice.dto.response.HighlightResponse;

import java.util.List;

public interface HighlightService {

    HighlightResponse addHighlight(HighlightRequest request);

    List<HighlightResponse> getHighlights(String collectionId, String paperId, String usersid);

    void deleteHighlight(String highlightId);
}

