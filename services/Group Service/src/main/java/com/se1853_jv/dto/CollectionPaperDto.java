package com.se1853_jv.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPaperDto {
    private String id;
    private String paperId;
    private String priority;
    private String status;
    private LocalDateTime addingDate;
}