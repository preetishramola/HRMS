package com.PreetishRamola.hrms.complaint;

import com.PreetishRamola.hrms.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    /**
     * Submit a complaint — any authenticated employee can submit.
     * The current user's identity is deliberately NOT forwarded to the service.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submit(
            @Valid @RequestBody ComplaintRequest req) {

        Complaint saved = complaintService.submit(req);
        // Return only the tracking ID — never echo back anything that could de-anonymise
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Complaint submitted anonymously",
                        Map.of("trackingId", saved.getId(),
                               "status", saved.getStatus().name())));
    }

    /** HR only: list all complaints */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<List<Complaint>>> getAll(
            @RequestParam(required = false) Complaint.ComplaintStatus status) {

        List<Complaint> list = status != null
                ? complaintService.getByStatus(status)
                : complaintService.getAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /** HR only: summary counts per status */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> summary() {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getSummary()));
    }

    /** HR only: update complaint status + add notes */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
    public ResponseEntity<ApiResponse<Complaint>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusRequest req) {

        return ResponseEntity.ok(ApiResponse.success("Status updated",
                complaintService.updateStatus(id, req)));
    }
}
