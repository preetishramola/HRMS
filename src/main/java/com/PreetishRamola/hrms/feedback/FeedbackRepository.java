package com.PreetishRamola.hrms.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /** All feedback received by a specific employee, newest first */
    @Query("SELECT f FROM Feedback f JOIN FETCH f.fromEmployee WHERE f.toEmployee.id = :toId ORDER BY f.createdAt DESC")
    List<Feedback> findByToEmployeeId(Long toId);

    /** All feedback given by a specific employee, newest first */
    @Query("SELECT f FROM Feedback f JOIN FETCH f.toEmployee WHERE f.fromEmployee.id = :fromId ORDER BY f.createdAt DESC")
    List<Feedback> findByFromEmployeeId(Long fromId);
}
