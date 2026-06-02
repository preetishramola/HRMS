package com.PreetishRamola.hrms.payroll;

import com.PreetishRamola.hrms.attendance.AttendanceRepository;
import com.PreetishRamola.hrms.employee.Employee;
import com.PreetishRamola.hrms.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    private static final BigDecimal WORKING_DAYS = BigDecimal.valueOf(26);
    private static final BigDecimal HRA_PERCENT = BigDecimal.valueOf(0.40);
    private static final BigDecimal ALLOWANCE_PERCENT = BigDecimal.valueOf(0.20);
    private static final BigDecimal PF_PERCENT = BigDecimal.valueOf(0.12);
    private static final BigDecimal TAX_PERCENT = BigDecimal.valueOf(0.10);

    public List<Payroll> generateMonthlyPayroll(int month, int year) {
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == Employee.EmploymentStatus.ACTIVE)
                .toList();

        return activeEmployees.stream()
                .map(emp -> generateForEmployee(emp, month, year))
                .toList();
    }

    public Payroll generateForEmployee(Employee employee, int month, int year) {
        // Skip if already generated
        Optional<Payroll> existing = payrollRepository
                .findByEmployeeIdAndMonthAndYear(employee.getId(), month, year);
        if (existing.isPresent()) return existing.get();

        long presentDays = attendanceRepository
                .countPresentDays(employee.getId(), month, year);

        BigDecimal presentDaysBD = BigDecimal.valueOf(Math.max(presentDays, 1));
        BigDecimal basic = employee.getSalary()
                .multiply(presentDaysBD)
                .divide(WORKING_DAYS, 2, RoundingMode.HALF_UP);

        BigDecimal hra = basic.multiply(HRA_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal allowances = basic.multiply(ALLOWANCE_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gross = basic.add(hra).add(allowances);

        BigDecimal pf = basic.multiply(PF_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = gross.multiply(TAX_PERCENT).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deductions = pf.add(tax);

        BigDecimal netPay = gross.subtract(deductions);

        Payroll payroll = Payroll.builder()
                .employee(employee)
                .month(month)
                .year(year)
                .basicSalary(basic)
                .hra(hra)
                .allowances(allowances)
                .deductions(deductions)
                .netPay(netPay)
                .workingDays(WORKING_DAYS)
                .presentDays(presentDaysBD)
                .build();

        return payrollRepository.save(payroll);
    }

    @Transactional(readOnly = true)
    public List<Payroll> getEmployeePayroll(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public Payroll getPayslip(Long employeeId, int month, int year) {
        return payrollRepository
                .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payslip not found for month " + month + "/" + year));
    }
}
