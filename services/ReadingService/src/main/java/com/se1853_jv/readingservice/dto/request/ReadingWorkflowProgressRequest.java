package com.se1853_jv.readingservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingWorkflowProgressRequest {

    @NotBlank
    @Size(max = 36)
    private String collectionId;

    @NotBlank
    @Size(max = 36)
    private String paperId;

    @NotBlank
    @Size(max = 36)
    private String usersid; // ERD uses "Usersid"

    @NotNull
    private Integer lastPage;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer progress;
}

