package com.se1853_jv.labverse.data.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionsPageResponse {
    private List<CollectionResponse> content;
    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
}


