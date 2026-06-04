package com.PreetishRamola.hrms.complaint;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Completely anonymous — no employee reference is ever stored.
 * The submitter's identity is deliberately discarded at the service layer.
 */
@Entity
@Table(name = "complaints")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintCategory category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    /** Internal notes added by HR — not visible to anyone else */
    @Column(columnDefinition = "TEXT")
    private String hrNotes;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime resolvedAt;

    public enum ComplaintCategory {
        HARASSMENT,
        DISCRIMINATION,
        MISCONDUCT,
        POLICY_VIOLATION,
        WORK_ENVIRONMENT,
        SAFETY,
        OTHER
    }

    public enum ComplaintStatus {
        OPEN,
        UNDER_REVIEW,
        RESOLVED,
        DISMISSED
    }
}
