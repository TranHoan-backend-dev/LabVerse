package com.se1853_jv.dto.response;

import com.se1853_jv.util.IdEncoder;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionPaperResponse {
    private String paperId; // encoded
    private String priority;
    private String status;
    private LocalDate addingDate;

    public static CollectionPaperResponse fromEntity(String paperId, String priority, String status, LocalDate addingDate) {
        return CollectionPaperResponse.builder()
                .paperId(IdEncoder.encode(paperId))
                .priority(priority)
                .status(status)
                .addingDate(addingDate)
                .build();
    }
}
