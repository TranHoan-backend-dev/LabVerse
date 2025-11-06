package com.se1853_jv.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionPaperRequest {
    @NotBlank
    private String collectionId;
    @NotBlank
    private String paperId;
    
    private String userId;    // User ID making the request (for authorization)

    private String priority;  // e.g. HIGH / MEDIUM / LOW
    private String status;    // e.g. ToRead / Reading / Finished
}


