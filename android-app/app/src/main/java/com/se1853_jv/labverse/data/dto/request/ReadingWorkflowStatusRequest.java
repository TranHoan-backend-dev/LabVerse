package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingWorkflowStatusRequest implements Serializable {
    private String collectionId; // Encoded
    private String paperId; // Encoded
    private String usersid; // Encoded
    private String status; // "unread", "reading", "finished"
}

