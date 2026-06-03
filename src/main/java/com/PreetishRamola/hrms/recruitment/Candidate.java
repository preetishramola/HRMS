package com.PreetishRamola.hrms.recruitment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private JobPosting jobPosting;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    // Cloudinary URL for resume PDF
    private String resumeUrl;

    // Plain text extracted from PDF at upload time — used by AI screening
    @Column(columnDefinition = "TEXT")
    private String resumeText;

    // AI screening results
    @Column(precision = 5, scale = 2)
    private BigDecimal aiScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal skillMatchPercent;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(columnDefinition = "TEXT")
    private String aiStrengths;   // JSON array stored as text

    @Column(columnDefinition = "TEXT")
    private String aiGaps;        // JSON array stored as text

    private String aiRecommendation;  // STRONG_YES, YES, MAYBE, NO

    // AI interview evaluation
    @Column(columnDefinition = "TEXT")
    private String interviewReport;

    private Integer interviewCommScore;     // /10
    private Integer interviewTechScore;     // /10
    private Integer interviewProblemScore;  // /10
    private String interviewRecommendation; // HIRE, CONSIDER, REJECT

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PipelineStage pipelineStage = PipelineStage.APPLIED;

    // Interview scheduling
    private LocalDateTime scheduledInterviewAt;
    private String interviewMeetingLink;

    // Offer letter
    @Column(unique = true)
    private String offerToken;             // UUID used in accept/decline links
    private LocalDateTime offerSentAt;

    @Enumerated(EnumType.STRING)
    private OfferStatus offerStatus;

    public enum OfferStatus {
        PENDING, ACCEPTED, DECLINED
    }

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime appliedAt;

    public enum PipelineStage {
        APPLIED, SCREENED, INTERVIEW, OFFER, HIRED, REJECTED
    }
}
