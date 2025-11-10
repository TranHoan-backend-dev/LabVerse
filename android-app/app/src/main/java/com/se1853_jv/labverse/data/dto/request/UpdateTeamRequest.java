package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamRequest implements Serializable {
    private String name;
    private String description;
    private String researchField;
    private String privacy; // PUBLIC or PRIVATE
    private String iconUrl;
}

