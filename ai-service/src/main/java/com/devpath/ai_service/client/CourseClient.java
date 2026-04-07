package com.devpath.ai_service.client;

import com.devpath.ai_service.dto.external.CourseDto;
import com.devpath.ai_service.dto.external.LessonDto;
import com.devpath.ai_service.dto.request.ContentCacheRequest;
import com.devpath.ai_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "course-service")
public interface CourseClient {

    @GetMapping("/api/courses")
    ApiResponse<List<CourseDto>> getAllCourses();

    @GetMapping("/api/courses/{id}/lessons/{lessonId}")
    ApiResponse<LessonDto> getLessonById(@PathVariable Long id,
                                         @PathVariable Long lessonId);

    @PutMapping("/api/courses/{id}/lessons/{lessonId}/cache")
    ApiResponse<Void> updateContentCache(@PathVariable Long id,
                                          @PathVariable Long lessonId,
                                          @RequestBody ContentCacheRequest request);
}
