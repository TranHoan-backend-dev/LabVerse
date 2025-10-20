package com.se1853_jv.util;


import com.se1853_jv.dto.*;
import com.se1853_jv.model.*;

import java.util.stream.Collectors;

public class MapperUtil {
    public static TeamDto toTeamDto(Team e) {
        return TeamDto.builder()
                .id(e.getId())
                .name(e.getName())
                .members(e.getMembers().stream().map(MapperUtil::toTeamMemberDto).collect(Collectors.toList()))
                .build();
    }

    public static TeamMemberDto toTeamMemberDto(UserTeam e) {
        return TeamMemberDto.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .role(e.getRole())
                .build();
    }

    public static CollectionDto toCollectionDto(Collection e) {
        return CollectionDto.builder()
                .id(e.getId())
                .name(e.getName())
                .members(e.getMembers().stream().map(MapperUtil::toCollectionMemberDto).collect(Collectors.toList()))
                .papers(e.getPapers().stream().map(MapperUtil::toCollectionPaperDto).collect(Collectors.toList()))
                .build();
    }

    public static CollectionMemberDto toCollectionMemberDto(CollectionUser e) {
        return CollectionMemberDto.builder()
                .id(e.getId())
                .memberId(e.getMemberId())
                .isAuthor(e.isAuthor())
                .build();
    }

    public static CollectionPaperDto toCollectionPaperDto(CollectionPaper e) {
        return CollectionPaperDto.builder()
                .id(e.getId())
                .paperId(e.getPaperId())
                .priority(e.getPriority())
                .status(e.getStatus())
                .addingDate(e.getAddingDate())
                .build();
    }
}
