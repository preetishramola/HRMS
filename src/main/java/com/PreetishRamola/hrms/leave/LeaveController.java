package com.PreetishRamola.hrms.leave;

import com.PreetishRamola.hrms.common.ApiResponse;
import com.PreetishRamola.hrms.employee.User;
import com.PreetishRamola.hrms.leave.dto.LeaveRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/api/employees/{id}/leaves")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveRequest>> applyLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave applied", leaveService.applyLeave(id, dto)));
    }

    @GetMapping("/api/employees/{id}/leaves")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaves(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getEmployeeLeaves(id)));
    }

    @GetMapping("/api/employees/{id}/leaves/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getLeaveBalance(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveBalance(id)));
    }

    @GetMapping("/api/leaves/pending")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getPending(
            @AuthenticationPrincipal User currentUser) {
        String role = currentUser.getRole().name();
        // HR and Admin see ALL company leaves; managers see only their direct reports
        boolean isHrOrAdmin = role.equals("ROLE_HR") || role.equals("ROLE_ADMIN");
        Long managerId = isHrOrAdmin ? null
                : (currentUser.getEmployee() != null ? currentUser.getEmployee().getId() : null);
        return ResponseEntity.ok(ApiResponse.success(leaveService.getPendingLeavesForManager(managerId)));
    }

    @PatchMapping("/api/leaves/{leaveId}/approve")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<LeaveRequest>> approve(
            @PathVariable Long leaveId,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getEmployee() == null) {
            throw new IllegalStateException("Approver must have a linked employee record");
        }
        return ResponseEntity.ok(ApiResponse.success("Leave approved",
                leaveService.approveLeave(leaveId, currentUser.getEmployee().getId())));
    }

    @PatchMapping("/api/leaves/{leaveId}/reject")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<LeaveRequest>> reject(
            @PathVariable Long leaveId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getEmployee() == null) {
            throw new IllegalStateException("Approver must have a linked employee record");
        }
        return ResponseEntity.ok(ApiResponse.success("Leave rejected",
                leaveService.rejectLeave(leaveId, currentUser.getEmployee().getId(), reason)));
    }
}
