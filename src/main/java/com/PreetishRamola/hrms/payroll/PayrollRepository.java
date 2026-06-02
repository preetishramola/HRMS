package com.PreetishRamola.hrms.payroll;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    List<Payroll> findByEmployeeId(Long employeeId);
    Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
    List<Payroll> findByMonthAndYear(int month, int year);

    @Query("SELECT SUM(p.netPay) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    Double sumNetPayByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
