package com.devpath.ai_service.dto.external;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LearningPathDto {
    private Long id;
    private Long userId;
    private String goal;
    private String level;
    private String hoursPerWeek;
    private LocalDateTime createdAt;
    private List<PathCourseDto> courses;
}
