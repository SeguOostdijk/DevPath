package com.devpath.enrollment.service;

import com.devpath.enrollment.client.CourseClient;
import com.devpath.enrollment.dto.external.CourseDto;
import com.devpath.enrollment.dto.request.CreatePathRequest;
import com.devpath.enrollment.dto.response.LearningPathResponse;
import com.devpath.enrollment.dto.response.PathCourseResponse;
import com.devpath.enrollment.exception.ResourceNotFoundException;
import com.devpath.enrollment.model.LearningPath;
import com.devpath.enrollment.model.PathCourse;
import com.devpath.enrollment.repository.LearningPathRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PathService {

    private final LearningPathRepository learningPathRepository;
    private final CourseClient courseClient;

    @Transactional
    public LearningPathResponse createPath(CreatePathRequest request) {
        LearningPath path = LearningPath.builder()
                .userId(request.getUserId())
                .goal(request.getGoal())
                .level(request.getLevel())
                .hoursPerWeek(request.getHoursPerWeek())
                .build();

        List<PathCourse> pathCourses = new ArrayList<>();
        for (int i = 0; i < request.getCourseIds().size(); i++) {
            pathCourses.add(PathCourse.builder()
                    .learningPath(path)
                    .courseId(request.getCourseIds().get(i))
                    .orderNumber(i + 1)
                    .build());
        }
        path.setPathCourses(pathCourses);

        path = learningPathRepository.save(path);
        return toResponse(path);
    }

    public List<LearningPathResponse> getMyPaths(String userEmail, Long userId) {
        return learningPathRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LearningPathResponse getPathById(Long userId, Long pathId) {
        LearningPath path = learningPathRepository.findById(pathId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path not found: " + pathId));

        if (!path.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Learning path not found: " + pathId);
        }

        return toResponse(path);
    }

    private LearningPathResponse toResponse(LearningPath path) {
        List<PathCourseResponse> courses = path.getPathCourses().stream()
                .map(pc -> {
                    CourseDto course = courseClient.getCourseById(pc.getCourseId()).getData();
                    return PathCourseResponse.builder()
                            .courseId(pc.getCourseId())
                            .courseTitle(course != null ? course.getTitle() : "Unknown")
                            .courseLevel(course != null ? course.getLevel() : null)
                            .orderNumber(pc.getOrderNumber())
                            .build();
                })
                .collect(Collectors.toList());

        return LearningPathResponse.builder()
                .id(path.getId())
                .userId(path.getUserId())
                .goal(path.getGoal())
                .level(path.getLevel())
                .hoursPerWeek(path.getHoursPerWeek())
                .createdAt(path.getCreatedAt())
                .courses(courses)
                .build();
    }
}
