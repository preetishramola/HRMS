package com.PreetishRamola.hrms.complaint;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findAllByOrderBySubmittedAtDesc();

    List<Complaint> findByStatusOrderBySubmittedAtDesc(Complaint.ComplaintStatus status);

    long countByStatus(Complaint.ComplaintStatus status);
}
