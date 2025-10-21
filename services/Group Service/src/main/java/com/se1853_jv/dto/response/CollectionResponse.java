package com.se1853_jv.dto.response;

import com.se1853_jv.model.Collection;
import com.se1853_jv.util.IdEncoder;
import lombok.*;


import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionResponse {
    private String id; // encoded
    private String name;
    private String paperIdsList;
    private List<CollectionMemberResponse> members;
    private List<CollectionPaperResponse> papers;

    public static CollectionResponse fromEntity(Collection entity) {
        return CollectionResponse.builder()
                .id(IdEncoder.encode(entity.getId()))
                .name(entity.getName())
                .paperIdsList(entity.getPaperIdsList())
                .members(entity.getMembers().stream()
                        .map(m -> CollectionMemberResponse.fromEntity(
                                m.getMemberId(),
                                Boolean.TRUE.equals(m.getIsAuthor())
                        ))
                        .collect(Collectors.toList()))
                .papers(entity.getPapers().stream()
                        .map(p -> CollectionPaperResponse.fromEntity(p.getPaperId(),
                                p.getPriority(),
                                p.getStatus(),
                                p.getAddingDate()))
                        .collect(Collectors.toList()))
                .build();
    }
}
