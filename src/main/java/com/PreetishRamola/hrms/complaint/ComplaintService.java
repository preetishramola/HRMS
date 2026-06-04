package com.PreetishRamola.hrms.complaint;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    /**
     * Submit a complaint. The caller's identity is intentionally NOT passed in —
     * this method only receives category + description, ensuring true anonymity.
     */
    public Complaint submit(ComplaintRequest req) {
        Complaint complaint = Complaint.builder()
                .category(req.getCategory())
                .description(req.getDescription())
                .status(Complaint.ComplaintStatus.OPEN)
                .build();

        Complaint saved = complaintRepository.save(complaint);
        log.info("[COMPLAINT] New anonymous complaint #{} submitted [{}]", saved.getId(), req.getCategory());
        return saved;
    }

    /** HR: get all complaints, newest first */
    @Transactional(readOnly = true)
    public List<Complaint> getAll() {
        return complaintRepository.findAllByOrderBySubmittedAtDesc();
    }

    /** HR: get complaints filtered by status */
    @Transactional(readOnly = true)
    public List<Complaint> getByStatus(Complaint.ComplaintStatus status) {
        return complaintRepository.findByStatusOrderBySubmittedAtDesc(status);
    }

    /** HR: get summary counts per status */
    @Transactional(readOnly = true)
    public Map<String, Long> getSummary() {
        return Map.of(
                "open",        complaintRepository.countByStatus(Complaint.ComplaintStatus.OPEN),
                "under_review", complaintRepository.countByStatus(Complaint.ComplaintStatus.UNDER_REVIEW),
                "resolved",    complaintRepository.countByStatus(Complaint.ComplaintStatus.RESOLVED),
                "dismissed",   complaintRepository.countByStatus(Complaint.ComplaintStatus.DISMISSED),
                "total",       complaintRepository.count()
        );
    }

    /** HR: update the status and add internal notes */
    public Complaint updateStatus(Long complaintId, ComplaintStatusRequest req) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found: " + complaintId));

        complaint.setStatus(req.getStatus());
        if (req.getHrNotes() != null && !req.getHrNotes().isBlank()) {
            complaint.setHrNotes(req.getHrNotes());
        }
        if (req.getStatus() == Complaint.ComplaintStatus.RESOLVED
                || req.getStatus() == Complaint.ComplaintStatus.DISMISSED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        log.info("[COMPLAINT] #{} status updated to {}", complaintId, req.getStatus());
        return complaintRepository.save(complaint);
    }
}
