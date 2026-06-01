package com.PreetishRamola.hrms.employee.dto;

import com.PreetishRamola.hrms.employee.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    private String phone;
    private String designation;
    private Long departmentId;
    private Long managerId;
    @NotNull private LocalDate joinDate;
    @NotNull @DecimalMin("0.0") private BigDecimal salary;
    private Role role;
    private String password;
}
