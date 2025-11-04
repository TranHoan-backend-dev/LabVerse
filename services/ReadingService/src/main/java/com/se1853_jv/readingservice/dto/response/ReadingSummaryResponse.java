package com.se1853_jv.readingservice.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReadingSummaryResponse {
    private Long unread;
    private Long reading;
    private Long finished;
}

