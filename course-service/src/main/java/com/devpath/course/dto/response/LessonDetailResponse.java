package com.devpath.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDetailResponse {
    private Long id;
    private int orderNumber;
    private String title;
    private String contentCache;
    private LocalDateTime contentGeneratedAt;
}
