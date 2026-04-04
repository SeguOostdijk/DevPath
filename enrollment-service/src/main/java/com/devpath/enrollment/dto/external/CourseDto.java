package com.devpath.enrollment.dto.external;

import lombok.Data;

@Data
public class CourseDto {

    private Long id;
    private String title;
    private String level;
    private Integer totalLessons;
    private Boolean active;
}
