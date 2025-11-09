package com.se1853_jv.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighlightResponse {
    private String id;
    private String color;
    private Long coordinationX;
    private Long coordinationY;
    private Integer pageNumber;
    private UUID paperId;
    private UUID collectionId;
    private UUID userId;
}

