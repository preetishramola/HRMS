package com.PreetishRamola.hrms.attendance;

import com.PreetishRamola.hrms.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/api/employees/{id}/attendance/checkin")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') and #id == authentication.principal.employee.id")
    public ResponseEntity<ApiResponse<Attendance>> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Checked in successfully",
                attendanceService.checkIn(id)));
    }

    @PatchMapping("/api/employees/{id}/attendance/checkout")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE') and #id == authentication.principal.employee.id")
    public ResponseEntity<ApiResponse<Attendance>> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully",
                attendanceService.checkOut(id)));
    }

    @GetMapping("/api/employees/{id}/attendance")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER') or #id == authentication.principal.employee.id")
    public ResponseEntity<ApiResponse<List<Attendance>>> getMonthlyAttendance(
            @PathVariable Long id,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getMonthlyAttendance(id, month, year)));
    }

    @GetMapping("/api/attendance/team")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<List<Attendance>>> getTeamAttendance(
            @RequestParam Long deptId) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getTodayTeamAttendance(deptId)));
    }

    @GetMapping("/api/employees/{id}/attendance/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceSummary(
            @PathVariable Long id,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.getAttendanceSummary(id, month, year)));
    }
}
