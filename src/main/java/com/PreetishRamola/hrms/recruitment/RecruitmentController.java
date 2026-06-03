package com.PreetishRamola.hrms.recruitment;

import com.PreetishRamola.hrms.common.ApiResponse;
import com.PreetishRamola.hrms.employee.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    @PostMapping("/api/jobs")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<JobPosting>> createJob(@RequestBody JobPosting job) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posted", recruitmentService.createJob(job)));
    }

    @GetMapping("/api/jobs")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<List<JobPosting>>> getJobs() {
        return ResponseEntity.ok(ApiResponse.success(recruitmentService.getAllJobs()));
    }

    @GetMapping("/api/jobs/{jobId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<JobPosting>> getJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(recruitmentService.getJobById(jobId)));
    }

    @PostMapping(value = "/api/jobs/{jobId}/candidates",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Candidate>> addCandidate(
            @PathVariable Long jobId,
            @RequestPart("candidate") Candidate candidate,
            @RequestPart(value = "resume", required = false) MultipartFile resumeFile) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidate added",
                        recruitmentService.addCandidate(jobId, candidate, resumeFile)));
    }

    // ── PUBLIC endpoints (no auth required) ──

    @GetMapping("/api/public/jobs")
    public ResponseEntity<ApiResponse<List<JobPosting>>> getPublicJobs() {
        return ResponseEntity.ok(ApiResponse.success(
                recruitmentService.getAllJobs().stream()
                        .filter(j -> j.getStatus() == JobPosting.JobStatus.OPEN)
                        .toList()));
    }

    @GetMapping("/api/public/jobs/{jobId}")
    public ResponseEntity<ApiResponse<JobPosting>> getPublicJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(recruitmentService.getJobById(jobId)));
    }

    @PostMapping(value = "/api/public/jobs/{jobId}/apply",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Candidate>> applyForJob(
            @PathVariable Long jobId,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestPart(value = "resumeFile", required = false) MultipartFile resumeFile) {
        Candidate candidate = new Candidate();
        candidate.setName(name);
        candidate.setEmail(email);
        candidate.setPhone(phone);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully",
                        recruitmentService.addCandidate(jobId, candidate, resumeFile)));
    }

    @GetMapping("/api/jobs/{jobId}/candidates")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<List<Candidate>>> getCandidates(@PathVariable Long jobId) {
        return ResponseEntity.ok(ApiResponse.success(
                recruitmentService.getCandidatesForJob(jobId)));
    }

    @PatchMapping("/api/candidates/{cId}/stage")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Candidate>> updateStage(
            @PathVariable Long cId,
            @RequestParam Candidate.PipelineStage stage) {
        return ResponseEntity.ok(ApiResponse.success("Stage updated",
                recruitmentService.updateCandidateStage(cId, stage)));
    }

    @PatchMapping("/api/candidates/{cId}/schedule-interview")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Candidate>> scheduleInterview(
            @PathVariable Long cId,
            @RequestBody Map<String, String> body) {
        LocalDateTime scheduledAt = LocalDateTime.parse(body.get("scheduledAt"));
        String meetingLink = body.get("meetingLink");
        return ResponseEntity.ok(ApiResponse.success("Interview scheduled",
                recruitmentService.scheduleInterview(cId, scheduledAt, meetingLink)));
    }

    @PostMapping("/api/candidates/{cId}/offer")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Candidate>> sendOffer(@PathVariable Long cId) {
        return ResponseEntity.ok(ApiResponse.success("Offer letter sent",
                recruitmentService.sendOfferLetter(cId)));
    }

    /** Public — candidate clicks Accept/Decline in email, lands on frontend /offer page,
     *  frontend calls this endpoint with the token */
    @PostMapping("/api/public/offer/respond")
    public ResponseEntity<ApiResponse<Candidate>> respondOffer(
            @RequestParam String token,
            @RequestParam boolean accepted) {
        return ResponseEntity.ok(ApiResponse.success(
                accepted ? "Offer accepted — welcome aboard!" : "Offer declined",
                recruitmentService.respondToOffer(token, accepted)));
    }

    /** Public — frontend fetches offer details to show candidate before they decide */
    @GetMapping("/api/public/offer")
    public ResponseEntity<ApiResponse<Candidate>> getOfferDetails(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(recruitmentService.getByOfferToken(token)));
    }

    @PostMapping("/api/candidates/{cId}/hire")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Employee>> hire(
            @PathVariable Long cId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long managerId) {
        return ResponseEntity.ok(ApiResponse.success("Candidate hired successfully",
                recruitmentService.hireCandidate(cId, departmentId, managerId)));
    }
}
