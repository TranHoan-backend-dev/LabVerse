package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AnnotationsResponse {
    private List<NoteResponse> notes;
    private List<HighlightResponse> highlights;
}

