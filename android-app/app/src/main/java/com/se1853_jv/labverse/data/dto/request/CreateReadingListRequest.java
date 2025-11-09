package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReadingListRequest implements Serializable {
    private String name;
    private List<String> userIdsList; // Optional: encoded user IDs
    private List<String> paperIdsList; // Optional: encoded paper IDs
}

