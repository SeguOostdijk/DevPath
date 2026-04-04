package com.devpath.enrollment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreatePathRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "goal is required")
    private String goal;

    private String level;

    private String hoursPerWeek;

    @NotNull(message = "courseIds is required")
    private List<Long> courseIds;
}
