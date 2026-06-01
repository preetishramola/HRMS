package com.PreetishRamola.hrms.employee;

import com.PreetishRamola.hrms.common.ApiResponse;
import com.PreetishRamola.hrms.common.PageResponse;
import com.PreetishRamola.hrms.employee.dto.EmployeeRequest;
import com.PreetishRamola.hrms.employee.dto.EmployeeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully",
                        employeeService.createEmployee(request)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.getAllEmployees(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.getById(id, currentUser)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success("Employee updated",
                employeeService.updateEmployee(id, request, currentUser)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(@PathVariable Long id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deactivated"));
    }

    /**
     * Lightweight directory for dropdowns (e.g. feedback recipient picker).
     * Returns id, firstName, lastName, designation, department only.
     * Available to all authenticated users.
     */
    /**
     * Returns all direct reports for the currently logged-in manager.
     * Used by the manager's performance review page to list team members.
     */
    @GetMapping("/my-team")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getMyTeam(
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = (User) currentUser;
        if (user.getEmployee() == null) {
            return ResponseEntity.ok(ApiResponse.success(PageResponse.from(
                    org.springframework.data.domain.Page.empty())));
        }
        Pageable all = PageRequest.of(0, 200, Sort.by("firstName").ascending());
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.getTeamEmployees(user.getEmployee().getId(), all)));
    }

    @GetMapping("/all/directory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> getDirectory() {
        Pageable all = PageRequest.of(0, 1000, Sort.by("firstName").ascending());
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.getAllEmployees(all)));
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getDashboardStats()));
    }
}
