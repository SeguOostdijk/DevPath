package com.devpath.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateContentCacheRequest {

    @NotBlank(message = "content is required")
    private String content;
}
