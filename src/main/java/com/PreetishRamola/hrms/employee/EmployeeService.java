package com.PreetishRamola.hrms.employee;

import com.PreetishRamola.hrms.common.PageResponse;
import com.PreetishRamola.hrms.department.Department;
import com.PreetishRamola.hrms.department.DepartmentRepository;
import com.PreetishRamola.hrms.employee.dto.EmployeeRequest;
import com.PreetishRamola.hrms.employee.dto.EmployeeResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered: " + request.getEmail());
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        }

        Employee manager = null;
        if (request.getManagerId() != null) {
            manager = employeeRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found"));
        }

        Employee employee = Employee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .designation(request.getDesignation())
                .department(department)
                .manager(manager)
                .joinDate(request.getJoinDate())
                .salary(request.getSalary())
                .build();

        employee = employeeRepository.save(employee);

        // Auto-create user account
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(
                        request.getPassword() != null ? request.getPassword() : "hrms@123"))
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_EMPLOYEE)
                .employee(employee)
                .build();

        userRepository.save(user);

        return EmployeeResponse.from(employee);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getAllEmployees(Pageable pageable) {
        Page<EmployeeResponse> page = employeeRepository.findAll(pageable)
                .map(EmployeeResponse::from);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> getTeamEmployees(Long managerId, Pageable pageable) {
        Page<EmployeeResponse> page = employeeRepository.findByManagerId(managerId, pageable)
                .map(EmployeeResponse::from);
        return PageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id, UserDetails currentUser) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        User user = (User) currentUser;
        // Employee can only view own profile
        if (user.getRole() == Role.ROLE_EMPLOYEE) {
            if (user.getEmployee() == null || !user.getEmployee().getId().equals(id)) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied");
            }
        }

        return EmployeeResponse.from(employee);
    }

    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request, UserDetails currentUser) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        User user = (User) currentUser;
        if (user.getRole() == Role.ROLE_EMPLOYEE) {
            if (user.getEmployee() == null || !user.getEmployee().getId().equals(id)) {
                throw new org.springframework.security.access.AccessDeniedException("Can only update own profile");
            }
        }

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setPhone(request.getPhone());
        employee.setDesignation(request.getDesignation());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
            employee.setDepartment(dept);
        }

        if (request.getSalary() != null && (user.getRole() == Role.ROLE_ADMIN)) {
            employee.setSalary(request.getSalary());
        }

        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        employee.setStatus(Employee.EmploymentStatus.INACTIVE);
        employeeRepository.save(employee);

        userRepository.findByEmployeeId(id).ifPresent(user -> {
            user.setActive(false);
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeRepository.count());
        stats.put("activeEmployees", employeeRepository.countByStatus(Employee.EmploymentStatus.ACTIVE));
        stats.put("totalDepartments", departmentRepository.count());
        return stats;
    }
}
