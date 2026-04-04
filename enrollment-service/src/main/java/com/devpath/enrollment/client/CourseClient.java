package com.devpath.enrollment.client;

import com.devpath.enrollment.dto.external.CourseDto;
import com.devpath.enrollment.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/courses/{id}")
    ApiResponse<CourseDto> getCourseById(@PathVariable Long id);
}
