package com.PreetishRamola.hrms.attendance;

import com.PreetishRamola.hrms.employee.Employee;
import com.PreetishRamola.hrms.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public Attendance checkIn(Long employeeId) {
        if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, LocalDate.now())) {
            throw new IllegalStateException("Already checked in today");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .date(LocalDate.now())
                .checkIn(LocalDateTime.now())
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        return attendanceRepository.save(attendance);
    }

    public Attendance checkOut(Long employeeId) {
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, LocalDate.now())
                .orElseThrow(() -> new IllegalStateException("No check-in found for today"));

        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("Already checked out today");
        }

        attendance.setCheckOut(LocalDateTime.now());

        // Calculate hours worked
        long minutes = ChronoUnit.MINUTES.between(attendance.getCheckIn(), attendance.getCheckOut());
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        attendance.setHoursWorked(hours);

        // Mark as half-day if worked less than 4 hours
        if (hours.compareTo(BigDecimal.valueOf(4)) < 0) {
            attendance.setStatus(Attendance.AttendanceStatus.HALF_DAY);
        }

        return attendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getMonthlyAttendance(Long employeeId, int month, int year) {
        return attendanceRepository.findByEmployeeAndMonth(employeeId, month, year);
    }

    @Transactional(readOnly = true)
    public List<Attendance> getTodayTeamAttendance(Long deptId) {
        return attendanceRepository.findByDepartmentAndDate(deptId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAttendanceSummary(Long employeeId, int month, int year) {
        long present = attendanceRepository.countPresentDays(employeeId, month, year);
        List<Attendance> records = attendanceRepository.findByEmployeeAndMonth(employeeId, month, year);
        return Map.of(
                "presentDays", present,
                "totalRecords", records.size(),
                "month", month,
                "year", year
        );
    }
}
