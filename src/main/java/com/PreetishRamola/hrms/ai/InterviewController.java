package com.PreetishRamola.hrms.ai;

import com.PreetishRamola.hrms.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;
    private final ResumeScreeningService resumeScreeningService;

    @PostMapping("/start/{candidateId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<InterviewSession>> startInterview(@PathVariable Long candidateId) {
        return ResponseEntity.ok(ApiResponse.success("Interview started",
                interviewService.startInterview(candidateId)));
    }

    @PostMapping("/{sessionId}/respond")
    public ResponseEntity<ApiResponse<InterviewSession>> respond(
            @PathVariable String sessionId,
            @RequestParam String answer) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.respond(sessionId, answer)));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<ApiResponse<InterviewSession>> endInterview(@PathVariable String sessionId) {
        InterviewSession result = interviewService.endInterview(sessionId);
        return ResponseEntity.ok(ApiResponse.success("Interview completed", result));
    }

    @PostMapping("/screen/{candidateId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<ScreeningResult>> screenResume(@PathVariable Long candidateId) {
        return ResponseEntity.ok(ApiResponse.success("Screening complete",
                resumeScreeningService.screenResume(candidateId)));
    }
}
