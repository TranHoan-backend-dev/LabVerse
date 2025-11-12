package com.se1853_jv.service;

import com.se1853_jv.dto.response.PaperProgressResponse;
import com.se1853_jv.dto.response.WrapperApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "READING-WORKFLOW-SERVICE", path = "/v1/api/progress")
public interface ReadingServiceClient {
    
    @GetMapping("/collection/{collectionId}/paper/{paperId}")
    WrapperApiResponse<PaperProgressResponse> getPaperProgress(
            @PathVariable("collectionId") String encodedCollectionId,
            @PathVariable("paperId") String encodedPaperId);
}

