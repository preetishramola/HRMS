package com.PreetishRamola.hrms.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {
    private String token;
    private String role;
    private Long userId;
    private Long employeeId;
    private String name;
    private String email;
}
