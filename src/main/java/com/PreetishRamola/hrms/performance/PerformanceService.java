package com.PreetishRamola.hrms.performance;

import com.PreetishRamola.hrms.employee.Employee;
import com.PreetishRamola.hrms.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final EmployeeRepository employeeRepository;

    public Performance createReview(Long employeeId, Long reviewerId, Performance request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
        Employee reviewer = employeeRepository.findById(reviewerId)
                .orElseThrow(() -> new EntityNotFoundException("Reviewer not found"));

        if (performanceRepository.findByEmployeeIdAndQuarterAndYear(
                employeeId, request.getQuarter(), request.getYear()).isPresent()) {
            throw new IllegalStateException("Review already exists for this quarter");
        }

        request.setEmployee(employee);
        request.setReviewer(reviewer);
        request.setStatus(Performance.ReviewStatus.DRAFT);
        return performanceRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<Performance> getEmployeeReviews(Long employeeId) {
        return performanceRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Performance> getReviewsByReviewer(Long reviewerId) {
        return performanceRepository.findByReviewerId(reviewerId);
    }

    public Performance submitReview(Long reviewId) {
        Performance review = performanceRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        review.setStatus(Performance.ReviewStatus.SUBMITTED);
        review.setSubmittedAt(LocalDateTime.now());
        return performanceRepository.save(review);
    }
}
