package com.devpath.ai_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SavePathRequest {
    private Long userId;
    private String goal;
    private String level;
    private String hoursPerWeek;
    private List<Long> courseIds;
}
