package com.devpath.enrollment.controller;

import com.devpath.enrollment.dto.request.EnrollmentRequest;
import com.devpath.enrollment.dto.response.ApiResponse;
import com.devpath.enrollment.dto.response.EnrollmentResponse;
import com.devpath.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> enroll(
            @RequestHeader("X-User-Id") String userEmail,
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enroll(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Enrolled successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getMyEnrollments(
            @RequestHeader("X-User-Id") String userEmail) {
        List<EnrollmentResponse> enrollments = enrollmentService.getMyEnrollments(userEmail);
        return ResponseEntity.ok(ApiResponse.success("Enrollments retrieved", enrollments));
    }

    @GetMapping("/me/{courseId}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentByCourse(
            @RequestHeader("X-User-Id") String userEmail,
            @PathVariable Long courseId) {
        EnrollmentResponse response = enrollmentService.getEnrollmentByCourse(userEmail, courseId);
        return ResponseEntity.ok(ApiResponse.success("Enrollment retrieved", response));
    }

    @PostMapping("/me/{courseId}/lessons/{lessonId}/complete")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> completeLesson(
            @RequestHeader("X-User-Id") String userEmail,
            @PathVariable Long courseId,
            @PathVariable Long lessonId) {
        EnrollmentResponse response = enrollmentService.completeLesson(userEmail, courseId, lessonId);
        return ResponseEntity.ok(ApiResponse.success("Lesson marked as completed", response));
    }
}
