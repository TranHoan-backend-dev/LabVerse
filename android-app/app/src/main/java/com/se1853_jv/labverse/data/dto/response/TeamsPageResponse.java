package com.se1853_jv.labverse.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamsPageResponse implements Serializable {
    private List<TeamResponse> content;
    private Pageable pageable;
    private Integer totalElements;
    private Integer totalPages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pageable implements Serializable {
        private Integer pageNumber;
        private Integer pageSize;
    }
}

