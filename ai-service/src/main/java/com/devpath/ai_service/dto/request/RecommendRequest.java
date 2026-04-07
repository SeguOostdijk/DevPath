package com.devpath.ai_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecommendRequest {

    @NotBlank(message = "goal is required")
    private String goal;

    private String level;

    private String hoursPerWeek;
}
