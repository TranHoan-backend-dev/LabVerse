package com.se1853_jv.dto.response;

import com.se1853_jv.model.CollectionPaper;
import com.se1853_jv.util.IdEncoder;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CollectionPaperResponse {
    private String collectionId;
    private String paperId;
    private String priority;
    private String status;
    private LocalDate addingDate;

    public static CollectionPaperResponse fromEntity(CollectionPaper entity) {
        return CollectionPaperResponse.builder()
                .collectionId(IdEncoder.encode(entity.getId().getCollectionId()))
                .paperId(IdEncoder.encode(entity.getId().getPaperId()))
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .addingDate(entity.getAddingDate())
                .build();
    }
}
