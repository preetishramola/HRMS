package com.PreetishRamola.hrms.leave;

import com.PreetishRamola.hrms.employee.Employee;
import com.PreetishRamola.hrms.employee.EmployeeRepository;
import com.PreetishRamola.hrms.leave.dto.LeaveRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveRequest applyLeave(Long employeeId, LeaveRequestDto dto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (dto.getFromDate().isAfter(dto.getToDate())) {
            throw new IllegalArgumentException("From date must be before to date");
        }

        LeaveRequest leave = LeaveRequest.builder()
                .employee(employee)
                .leaveType(dto.getLeaveType())
                .fromDate(dto.getFromDate())
                .toDate(dto.getToDate())
                .reason(dto.getReason())
                .status(LeaveRequest.LeaveStatus.PENDING)
                .build();

        return leaveRepository.save(leave);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmployeeLeaves(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    /**
     * HR/Admin: managerId == null → return ALL company leaves.
     * Manager: managerId set → return only their direct reports' pending leaves.
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeavesForManager(Long managerId) {
        if (managerId == null) {
            return leaveRepository.findAllOrderByCreatedAtDesc();
        }
        return leaveRepository.findPendingByManagerId(managerId);
    }

    public LeaveRequest approveLeave(Long leaveId, Long approverId) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found"));

        if (leave.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is already " + leave.getStatus());
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("Approver not found"));

        leave.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leave.setApprovedBy(approver);
        leave.setActionedAt(LocalDateTime.now());

        return leaveRepository.save(leave);
    }

    public LeaveRequest rejectLeave(Long leaveId, Long approverId, String reason) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new EntityNotFoundException("Leave request not found"));

        if (leave.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is already " + leave.getStatus());
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new EntityNotFoundException("Approver not found"));

        leave.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leave.setApprovedBy(approver);
        leave.setRejectionReason(reason);
        leave.setActionedAt(LocalDateTime.now());

        return leaveRepository.save(leave);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getLeaveBalance(Long employeeId) {
        int currentYear = java.time.LocalDate.now().getYear();
        return Map.of(
                "casual", Math.max(0, 12 - leaveRepository.countApprovedLeavesByType(
                        employeeId, LeaveRequest.LeaveType.CASUAL, currentYear)),
                "sick", Math.max(0, 7 - leaveRepository.countApprovedLeavesByType(
                        employeeId, LeaveRequest.LeaveType.SICK, currentYear)),
                "earned", Math.max(0, 15 - leaveRepository.countApprovedLeavesByType(
                        employeeId, LeaveRequest.LeaveType.EARNED, currentYear))
        );
    }
}
