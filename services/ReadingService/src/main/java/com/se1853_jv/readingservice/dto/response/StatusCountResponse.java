package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StatusCountResponse {
    private String status;
    private Long count;
    private Double percentage; // Percentage of total workflows
}













