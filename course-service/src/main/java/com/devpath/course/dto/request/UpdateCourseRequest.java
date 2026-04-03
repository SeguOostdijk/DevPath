package com.devpath.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCourseRequest {

    @NotNull(message = "categoryId es requerido")
    private Long categoryId;

    @NotNull(message = "instructorId es requerido")
    private Long instructorId;

    @NotBlank(message = "title es requerido")
    private String title;

    private String description;

    @NotBlank(message = "level es requerido")
    private String level;

    @NotNull(message = "active es requerido")
    private Boolean active;
}
