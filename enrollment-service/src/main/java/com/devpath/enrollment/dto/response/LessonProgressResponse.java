package com.devpath.enrollment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonProgressResponse {

    private Long lessonId;
    private Boolean completed;
    private LocalDateTime completedAt;
}
