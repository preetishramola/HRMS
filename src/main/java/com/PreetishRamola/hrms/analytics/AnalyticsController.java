package com.PreetishRamola.hrms.analytics;

import com.PreetishRamola.hrms.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_HR')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> overview() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getOverview()));
    }

    @GetMapping("/hiring-funnel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hiringFunnel() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getHiringFunnel()));
    }

    @GetMapping("/department-stats")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> departmentStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDepartmentStats()));
    }

    @GetMapping("/attendance-trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> attendanceTrends() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAttendanceTrends()));
    }

    @GetMapping("/leave-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> leaveStats() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getLeaveStats()));
    }

    @GetMapping("/performance-overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> performanceOverview() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getPerformanceOverview()));
    }

    @GetMapping("/ai-insights")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> aiInsights() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAiInsights()));
    }

    @GetMapping("/attrition-risk")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> attritionRisk() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getAttritionRisk()));
    }
}
