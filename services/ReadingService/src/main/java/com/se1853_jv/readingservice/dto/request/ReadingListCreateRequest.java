package com.se1853_jv.readingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReadingListCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private List<String> userIdsList; // Will be stored as JSON string

    private List<String> paperIdsList; // Will be stored as JSON string
}

