package com.PreetishRamola.hrms.performance;

import com.PreetishRamola.hrms.common.ApiResponse;
import com.PreetishRamola.hrms.employee.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping("/api/employees/{id}/performance")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Performance>> createReview(
            @PathVariable Long id,
            @RequestBody Performance request,
            @AuthenticationPrincipal User currentUser) {
        Long reviewerId = currentUser.getEmployee().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created",
                        performanceService.createReview(id, reviewerId, request)));
    }

    @GetMapping("/api/employees/{id}/performance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Performance>>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.getEmployeeReviews(id)));
    }

    @GetMapping("/api/performance/team")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<List<Performance>>> getTeamReviews(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                performanceService.getReviewsByReviewer(currentUser.getEmployee().getId())));
    }

    @PatchMapping("/api/performance/{reviewId}/submit")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<ApiResponse<Performance>> submitReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(ApiResponse.success("Review submitted",
                performanceService.submitReview(reviewId)));
    }
}
