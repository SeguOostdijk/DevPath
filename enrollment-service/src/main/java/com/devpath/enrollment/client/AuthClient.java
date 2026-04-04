package com.devpath.enrollment.client;

import com.devpath.enrollment.dto.external.UserDto;
import com.devpath.enrollment.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/api/auth/me")
    ApiResponse<UserDto> getUserByEmail(@RequestHeader("X-User-Id") String email);
}
