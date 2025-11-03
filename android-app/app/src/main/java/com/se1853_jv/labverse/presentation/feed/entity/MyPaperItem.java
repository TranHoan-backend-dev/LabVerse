package com.se1853_jv.labverse.presentation.feed.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MyPaperItem {
    private List<Paper> papers;
    private RecentActivity recentActivity;
    private Summary summary;
}
