package com.se1853_jv.service;

import com.se1853_jv.dto.response.WrapperApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "GROUP-SERVICE", path = "/v1/api/collections")
public interface GroupServiceClient {
    
    @GetMapping("/internal/statistics")
    WrapperApiResponse getCollectionStatistics();
}

