package com.se1853_jv.dto.request;


import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPaperRequest {
    private String paperId;
    private String priority;
    private String status;
    private LocalDate addingDate;
}
