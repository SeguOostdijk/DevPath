package com.devpath.ai_service.dto.external;

import lombok.Data;

@Data
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private String level;
    private int totalLessons;
    private String category;
    private String instructor;
}
