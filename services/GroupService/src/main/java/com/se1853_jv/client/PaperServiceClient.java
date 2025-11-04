package com.se1853_jv.client;

import com.se1853_jv.dto.response.PaperResponse;
import com.se1853_jv.dto.response.WrapperApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "PAPER-SERVICE", path = "/v1/api")
public interface PaperServiceClient {
    
    @GetMapping("/papers/details")
    WrapperApiResponse getPaperDetails(@RequestParam("id") String encodedPaperId);
}

