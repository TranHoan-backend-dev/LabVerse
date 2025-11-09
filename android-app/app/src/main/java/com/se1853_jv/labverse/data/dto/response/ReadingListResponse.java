package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingListResponse implements Serializable {
    private String id; // Encoded ID
    private String name;
    private List<String> userIdsList; // Encoded user IDs
    private List<String> paperIdsList; // Encoded paper IDs
    private String createdAt;
    private String updatedAt;
}

