package com.se1853_jv.readingservice.service;

import com.se1853_jv.readingservice.dto.response.AnnotationsResponse;
import com.se1853_jv.readingservice.dto.response.ReadingSummaryResponse;

public interface SummaryService {

    ReadingSummaryResponse getReadingSummary(String usersid);

    AnnotationsResponse getAnnotations(String paperId, String usersid);
}

