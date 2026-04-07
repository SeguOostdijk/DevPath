package com.devpath.ai_service.dto.external;

import lombok.Data;

@Data
public class PathCourseDto {
    private Long courseId;
    private String courseTitle;
    private String courseLevel;
    private Integer orderNumber;
}
