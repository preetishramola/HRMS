package com.PreetishRamola.hrms.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);
    Page<Employee> findByManagerId(Long managerId, Pageable pageable);
    List<Employee> findByManagerId(Long managerId);
    List<Employee> findByDepartmentId(Long departmentId);
    long countByStatus(Employee.EmploymentStatus status);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :deptId AND e.manager.id = :managerId")
    List<Employee> findByDepartmentAndManager(@Param("deptId") Long deptId, @Param("managerId") Long managerId);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :deptId")
    long countByDepartmentId(@Param("deptId") Long deptId);
}
