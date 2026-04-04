package com.devpath.enrollment.controller;

import com.devpath.enrollment.client.AuthClient;
import com.devpath.enrollment.dto.external.UserDto;
import com.devpath.enrollment.dto.request.CreatePathRequest;
import com.devpath.enrollment.dto.response.ApiResponse;
import com.devpath.enrollment.dto.response.LearningPathResponse;
import com.devpath.enrollment.exception.ResourceNotFoundException;
import com.devpath.enrollment.service.PathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paths")
@RequiredArgsConstructor
public class PathController {

    private final PathService pathService;
    private final AuthClient authClient;

    // INTERNAL — solo llamado por ai-service
    @PostMapping
    public ResponseEntity<ApiResponse<LearningPathResponse>> createPath(
            @Valid @RequestBody CreatePathRequest request) {
        LearningPathResponse response = pathService.createPath(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Learning path created", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<LearningPathResponse>>> getMyPaths(
            @RequestHeader("X-User-Id") String userEmail) {
        UserDto user = authClient.getUserByEmail(userEmail).getData();
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + userEmail);
        }
        List<LearningPathResponse> paths = pathService.getMyPaths(userEmail, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Learning paths retrieved", paths));
    }

    @GetMapping("/me/{pathId}")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getPathById(
            @RequestHeader("X-User-Id") String userEmail,
            @PathVariable Long pathId) {
        UserDto user = authClient.getUserByEmail(userEmail).getData();
        if (user == null) {
            throw new ResourceNotFoundException("User not found: " + userEmail);
        }
        LearningPathResponse path = pathService.getPathById(user.getId(), pathId);
        return ResponseEntity.ok(ApiResponse.success("Learning path retrieved", path));
    }
}
