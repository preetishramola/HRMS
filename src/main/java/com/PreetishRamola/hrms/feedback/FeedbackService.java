package com.PreetishRamola.hrms.feedback;

import com.PreetishRamola.hrms.employee.Employee;
import com.PreetishRamola.hrms.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EmployeeRepository employeeRepository;

    public Feedback giveFeedback(Long fromEmployeeId, FeedbackRequest req) {
        if (fromEmployeeId.equals(req.getToEmployeeId())) {
            throw new IllegalArgumentException("You cannot give feedback to yourself");
        }

        Employee from = employeeRepository.findById(fromEmployeeId)
                .orElseThrow(() -> new EntityNotFoundException("Sender employee not found"));
        Employee to = employeeRepository.findById(req.getToEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Recipient employee not found"));

        Feedback feedback = Feedback.builder()
                .fromEmployee(from)
                .toEmployee(to)
                .category(req.getCategory())
                .content(req.getContent())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        log.info("Feedback submitted: {} → {} [{}]", from.getEmail(), to.getEmail(), req.getCategory());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Feedback> getReceivedFeedback(Long employeeId) {
        return feedbackRepository.findByToEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Feedback> getGivenFeedback(Long employeeId) {
        return feedbackRepository.findByFromEmployeeId(employeeId);
    }
}
