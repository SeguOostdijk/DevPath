package com.devpath.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLessonRequest {

    @NotNull(message = "orderNumber es requerido")
    @Min(value = 1, message = "orderNumber debe ser mayor a 0")
    private Integer orderNumber;

    @NotBlank(message = "title es requerido")
    private String title;
}
