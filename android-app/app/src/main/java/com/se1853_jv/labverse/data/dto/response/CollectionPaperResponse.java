package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionPaperResponse {
    private String collectionId;
    private String paperId;
    private String priority;
    private String status;
    private String addingDate;
}

