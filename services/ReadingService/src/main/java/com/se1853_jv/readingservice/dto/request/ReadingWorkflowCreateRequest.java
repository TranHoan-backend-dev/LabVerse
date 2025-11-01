package com.se1853_jv.readingservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadingWorkflowCreateRequest {

    // Optional - có thể để null, sẽ tự generate hoặc set sau
    @Size(max = 36)
    private String collectionId;

    @Size(max = 36)
    private String paperId;

    @Size(max = 36)
    private String userId;

    @Size(max = 255)
    private String status;

    private Integer lastPage;

    @Min(0)
    @Max(100)
    private Integer progress;
}

