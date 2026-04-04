package com.devpath.enrollment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PathCourseResponse {

    private Long courseId;
    private String courseTitle;
    private String courseLevel;
    private Integer orderNumber;
}
