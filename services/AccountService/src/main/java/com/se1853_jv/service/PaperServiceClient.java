package com.se1853_jv.service;

import com.se1853_jv.dto.response.WrapperApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "PAPER-SERVICE", path = "/v1/api")
public interface PaperServiceClient {
    
    @GetMapping("/internal/api/papers/statistics")
    WrapperApiResponse getPaperStatistics();
}

