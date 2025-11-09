package com.se1853_jv.labverse.presentation.feed.entity;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Summary {
    private int papers, collections;
    @JsonAlias("team-projects")
    private int teamProject;
}
