package com.devpath.ai_service.client;

import com.devpath.ai_service.dto.external.LearningPathDto;
import com.devpath.ai_service.dto.request.SavePathRequest;
import com.devpath.ai_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "enrollment-service")
public interface EnrollmentClient {

    @PostMapping("/api/paths")
    ApiResponse<LearningPathDto> savePath(@RequestBody SavePathRequest request);
}
