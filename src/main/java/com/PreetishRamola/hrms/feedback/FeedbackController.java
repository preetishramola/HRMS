package com.PreetishRamola.hrms.feedback;

import com.PreetishRamola.hrms.common.ApiResponse;
import com.PreetishRamola.hrms.employee.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /** Any authenticated user can give feedback to any other employee */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Feedback>> give(
            @Valid @RequestBody FeedbackRequest req,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getEmployee() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No employee record linked to your account"));
        }
        Feedback saved = feedbackService.giveFeedback(currentUser.getEmployee().getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted", saved));
    }

    /** Get feedback received by the currently logged-in employee (their inbox) */
    @GetMapping("/received")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Feedback>>> received(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getEmployee() == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.getReceivedFeedback(currentUser.getEmployee().getId())));
    }

    /** Get feedback the currently logged-in employee has given (their sent box) */
    @GetMapping("/given")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Feedback>>> given(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getEmployee() == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                feedbackService.getGivenFeedback(currentUser.getEmployee().getId())));
    }
}
