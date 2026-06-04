package com.PreetishRamola.hrms.feedback;

import com.PreetishRamola.hrms.employee.Employee;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The person who wrote the feedback */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee fromEmployee;

    /** The person the feedback is about — only they can see it */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_employee_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Employee toEmployee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedbackCategory category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum FeedbackCategory {
        RECOGNITION,      // Great job / shoutout
        IMPROVEMENT,      // Constructive suggestion
        COLLABORATION,    // Teamwork related
        GENERAL           // General note
    }
}
