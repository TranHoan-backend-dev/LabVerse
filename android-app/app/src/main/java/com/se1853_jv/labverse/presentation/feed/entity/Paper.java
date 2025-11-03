package com.se1853_jv.labverse.presentation.feed.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Paper implements Serializable {
    private String id, title, authors, journal, status;
    private int year;
}
