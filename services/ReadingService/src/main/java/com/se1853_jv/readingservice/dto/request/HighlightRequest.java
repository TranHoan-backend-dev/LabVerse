package com.se1853_jv.readingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HighlightRequest {

    @NotBlank
    @Size(max = 50)
    private String color;

    @NotNull
    private Integer coordinationX;

    @NotNull
    private Integer coordinationY;

    @NotNull
    private Integer pageNumber;

    @NotBlank
    @Size(max = 36)
    private String collectionId;

    @NotBlank
    @Size(max = 36)
    private String paperId;

    @NotBlank
    @Size(max = 36)
    private String usersid; // ERD uses "Usersid"
}

