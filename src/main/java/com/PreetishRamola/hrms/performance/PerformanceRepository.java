package com.PreetishRamola.hrms.performance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findByEmployeeId(Long employeeId);
    Optional<Performance> findByEmployeeIdAndQuarterAndYear(Long employeeId, int quarter, int year);
    List<Performance> findByReviewerId(Long reviewerId);

    @Query("SELECT AVG(p.rating) FROM Performance p WHERE p.employee.department.id = :deptId AND p.year = :year")
    Double avgRatingByDepartment(@Param("deptId") Long deptId, @Param("year") int year);
}
