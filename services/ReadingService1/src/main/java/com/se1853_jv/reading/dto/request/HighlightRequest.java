package com.se1853_jv.reading.dto.request;

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
}

