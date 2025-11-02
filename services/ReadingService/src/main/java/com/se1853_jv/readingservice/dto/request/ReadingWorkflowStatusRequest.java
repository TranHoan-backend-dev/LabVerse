package com.se1853_jv.readingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingWorkflowStatusRequest {

    @NotBlank
    @Size(max = 36)
    private String collectionId;

    @NotBlank
    @Size(max = 36)
    private String paperId;

    @NotBlank
    @Size(max = 36)
    private String usersid; // ERD uses "Usersid"

    @NotBlank
    @Size(max = 10)
    private String status; // "unread" | "reading" | "finished"
}

