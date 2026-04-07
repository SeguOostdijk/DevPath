package com.devpath.ai_service.client;

import com.devpath.ai_service.dto.external.UserDto;
import com.devpath.ai_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/auth/me")
    ApiResponse<UserDto> getUserByEmail(@RequestHeader("X-User-Id") String email);
}
