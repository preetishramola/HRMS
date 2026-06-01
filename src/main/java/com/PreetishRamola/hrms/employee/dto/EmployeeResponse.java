package com.PreetishRamola.hrms.employee.dto;

import com.PreetishRamola.hrms.employee.Employee;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String designation;
    private String departmentName;
    private Long departmentId;
    private String managerName;
    private Long managerId;
    private LocalDate joinDate;
    private BigDecimal salary;
    private String status;
    private String profileImageUrl;

    public static EmployeeResponse from(Employee e) {
        return EmployeeResponse.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .designation(e.getDesignation())
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .managerName(e.getManager() != null ? e.getManager().getFullName() : null)
                .managerId(e.getManager() != null ? e.getManager().getId() : null)
                .joinDate(e.getJoinDate())
                .salary(e.getSalary())
                .status(e.getStatus().name())
                .profileImageUrl(e.getProfileImageUrl())
                .build();
    }
}
