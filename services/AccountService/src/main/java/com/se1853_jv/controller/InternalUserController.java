package com.se1853_jv.controller;

import com.se1853_jv.dto.response.WrapperApiResponse;
import com.se1853_jv.service.UserService;
import com.se1853_jv.util.IdEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal controller for service-to-service communication
 * This endpoint should not require authentication and should only be accessible from internal services
 */
@RestController
@RequestMapping("/internal/api/users")
public class InternalUserController {

    private final UserService userService;

    @Autowired
    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WrapperApiResponse> getUserById(@PathVariable String id) {
        String decodedId = IdEncoder.decode(id);
        if (decodedId == null) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, "Invalid user ID format"));
        }
        try {
            return ResponseEntity.ok(WrapperApiResponse.success(userService.getUserById(decodedId)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(WrapperApiResponse.error(400, e.getMessage()));
        }
    }
}

