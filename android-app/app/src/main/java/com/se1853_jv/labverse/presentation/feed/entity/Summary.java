package com.se1853_jv.labverse.presentation.feed.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Summary {
    private int papers, collections;
    private int teamProjects;
}
