package com.devpath.enrollment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LearningPathResponse {

    private Long id;
    private Long userId;
    private String goal;
    private String level;
    private String hoursPerWeek;
    private LocalDateTime createdAt;
    private List<PathCourseResponse> courses;
}
