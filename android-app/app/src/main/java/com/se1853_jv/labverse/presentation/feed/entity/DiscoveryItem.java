package com.se1853_jv.labverse.presentation.feed.entity;

import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;
import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiscoveryItem {
    private int id;
    private List<Tag> tags;
    private String title;
    private String summary;
    private String journal;
    private String timeAgo;
    private int citations;
    private Users author;
}
