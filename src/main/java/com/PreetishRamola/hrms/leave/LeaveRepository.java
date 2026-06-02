package com.PreetishRamola.hrms.leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveRequest.LeaveStatus status);

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.manager.id = :managerId AND l.status = 'PENDING'")
    List<LeaveRequest> findPendingByManagerId(@Param("managerId") Long managerId);

    /** HR: all pending leaves across the entire company */
    @Query("SELECT l FROM LeaveRequest l ORDER BY l.createdAt DESC")
    List<LeaveRequest> findAllOrderByCreatedAtDesc();

    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.department.id = :deptId AND l.status = 'PENDING'")
    List<LeaveRequest> findPendingByDepartmentId(@Param("deptId") Long deptId);

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.employee.id = :empId AND l.leaveType = :type AND l.status = 'APPROVED' AND YEAR(l.fromDate) = :year")
    long countApprovedLeavesByType(@Param("empId") Long empId, @Param("type") LeaveRequest.LeaveType type, @Param("year") int year);

    @Query("SELECT l.leaveType, COUNT(l) FROM LeaveRequest l WHERE l.status = 'APPROVED' AND YEAR(l.fromDate) = :year GROUP BY l.leaveType")
    List<Object[]> countApprovedByTypeForYear(@Param("year") int year);

    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = 'PENDING'")
    long countPending();

    @Query("SELECT l.employee.department.name, COUNT(l) FROM LeaveRequest l WHERE l.status = 'APPROVED' AND YEAR(l.fromDate) = :year GROUP BY l.employee.department.name")
    List<Object[]> countApprovedByDepartment(@Param("year") int year);
}
