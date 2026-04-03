package com.devpath.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private String level;
    private int totalLessons;
    private CategoryResponse category;
    private InstructorResponse instructor;
    private boolean active;
}
