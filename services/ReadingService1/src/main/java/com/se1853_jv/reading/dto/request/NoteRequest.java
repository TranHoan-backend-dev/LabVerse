package com.se1853_jv.reading.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteRequest {

    @NotBlank
    @Size(max = 255)
    private String content;

    @NotNull
    private Integer coordinationX;

    @NotNull
    private Integer coordinationY;

    @NotNull
    private Integer pageNumber;
}

