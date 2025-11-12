package com.se1853_jv.service;

import com.se1853_jv.dto.response.WrapperApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ACCOUNT-SERVICE")
public interface UserServiceClient {
    
    @GetMapping("/internal/api/users/{id}")
    WrapperApiResponse getUserById(@PathVariable("id") String encodedUserId);
    
    @GetMapping("/internal/api/users/email/{email}")
    WrapperApiResponse getUserByEmail(@PathVariable("email") String email);
}

