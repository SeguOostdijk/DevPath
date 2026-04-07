package com.devpath.ai_service.dto.external;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String role;
}
