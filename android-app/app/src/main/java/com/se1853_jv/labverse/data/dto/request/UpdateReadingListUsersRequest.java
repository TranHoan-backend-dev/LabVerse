package com.se1853_jv.labverse.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReadingListUsersRequest implements Serializable {
    private String action; // "add" or "remove"
    private List<String> userIds; // Encoded user IDs
}

