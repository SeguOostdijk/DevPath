package com.devpath.enrollment.dto.response;

import com.devpath.enrollment.model.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private EnrollmentStatus status;
    private Integer progressPct;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
    private List<LessonProgressResponse> lessonProgresses;
}
