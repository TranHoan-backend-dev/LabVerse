package com.se1853_jv.readingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReadingListUpdateUsersRequest {

    @NotBlank
    private String action; // "add" | "remove"

    @jakarta.validation.constraints.NotNull
    private List<String> userIds;
}

