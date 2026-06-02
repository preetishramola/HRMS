package com.PreetishRamola.hrms.payroll;

import com.PreetishRamola.hrms.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollService payrollService;

    @PostMapping("/api/payroll/generate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<Payroll>>> generate(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success("Payroll generated",
                payrollService.generateMonthlyPayroll(month, year)));
    }

    @GetMapping("/api/employees/{id}/payroll")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.employee.id")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayroll(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getEmployeePayroll(id)));
    }

    @GetMapping("/api/employees/{id}/payroll/{month}/{year}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #id == authentication.principal.employee.id")
    public ResponseEntity<ApiResponse<Payroll>> getPayslip(
            @PathVariable Long id, @PathVariable int month, @PathVariable int year) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayslip(id, month, year)));
    }
}
