package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPaperRequest {
    private String collectionId;
    private String paperId;
    private String priority;  // e.g. HIGH / MEDIUM / LOW
    private String status;    // e.g. ToRead / Reading / Finished
}

