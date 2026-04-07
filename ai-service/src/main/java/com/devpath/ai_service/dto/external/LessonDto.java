package com.devpath.ai_service.dto.external;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LessonDto {
    private Long id;
    private int orderNumber;
    private String title;
    private String contentCache;
    private LocalDateTime contentGeneratedAt;
}
