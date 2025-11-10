package com.se1853_jv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateNoteRequest {
    @NotBlank(message = "Paper ID is required")
    private String paperId;
    
    @NotBlank(message = "Collection ID is required")
    private String collectionId;
    
    @NotBlank(message = "Note content is required")
    private String content;
    
    @NotNull(message = "X coordinate is required")
    private Integer coordinationX;
    
    @NotNull(message = "Y coordinate is required")
    private Integer coordinationY;
    
    @NotNull(message = "Page number is required")
    private Integer pageNumber;
}

