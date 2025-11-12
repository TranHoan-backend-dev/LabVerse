package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingWorkflowProgressRequest implements Serializable {
    private String collectionId; // Encoded
    private String paperId; // Encoded
    private String usersid; // Encoded
    private Integer lastPage;
    private Integer progress; // 0-100
}

