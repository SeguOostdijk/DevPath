package com.devpath.enrollment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;
}
