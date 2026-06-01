package com.PreetishRamola.hrms.department;

import com.PreetishRamola.hrms.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Department>> create(@RequestBody Department request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created", departmentService.create(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Department>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Department>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Department>> update(@PathVariable Long id, @RequestBody Department req) {
        return ResponseEntity.ok(ApiResponse.success("Updated", departmentService.update(id, req)));
    }
}
