package com.PreetishRamola.hrms.recruitment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByStatus(JobPosting.JobStatus status);
    long countByStatus(JobPosting.JobStatus status);
    List<JobPosting> findByDepartmentId(Long departmentId);
}
