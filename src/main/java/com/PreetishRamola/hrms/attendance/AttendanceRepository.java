package com.PreetishRamola.hrms.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :empId AND MONTH(a.date) = :month AND YEAR(a.date) = :year")
    List<Attendance> findByEmployeeAndMonth(@Param("empId") Long empId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT a FROM Attendance a WHERE a.employee.department.id = :deptId AND a.date = :date")
    List<Attendance> findByDepartmentAndDate(@Param("deptId") Long deptId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :empId AND a.status = 'PRESENT' AND MONTH(a.date) = :month AND YEAR(a.date) = :year")
    long countPresentDays(@Param("empId") Long empId, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.status = 'PRESENT' AND MONTH(a.date) = :month AND YEAR(a.date) = :year")
    long countTotalPresentDays(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(DISTINCT a.employee.id) FROM Attendance a WHERE a.date = :date")
    long countDistinctEmployeesByDate(@Param("date") java.time.LocalDate date);

    @Query("SELECT a.date, COUNT(a) FROM Attendance a WHERE a.status = 'PRESENT' AND a.date >= :from AND a.date <= :to GROUP BY a.date ORDER BY a.date")
    List<Object[]> countPresentByDateRange(@Param("from") java.time.LocalDate from, @Param("to") java.time.LocalDate to);
}
