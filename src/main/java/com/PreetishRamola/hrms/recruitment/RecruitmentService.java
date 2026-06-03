package com.PreetishRamola.hrms.recruitment;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.PreetishRamola.hrms.department.Department;
import com.PreetishRamola.hrms.department.DepartmentRepository;
import com.PreetishRamola.hrms.employee.Employee;
import com.PreetishRamola.hrms.employee.EmployeeRepository;
import com.PreetishRamola.hrms.employee.Role;
import com.PreetishRamola.hrms.employee.User;
import com.PreetishRamola.hrms.employee.UserRepository;
import com.PreetishRamola.hrms.ai.ScreeningTriggerService;
import com.PreetishRamola.hrms.notification.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class RecruitmentService {

    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;
    private final NotificationService notificationService;

    // @Lazy breaks the circular dependency:
    // ResumeScreeningService → RecruitmentService → ScreeningTriggerService → ResumeScreeningService
    @Autowired @Lazy
    private ScreeningTriggerService screeningTriggerService;

    public RecruitmentService(JobPostingRepository jobPostingRepository,
                               CandidateRepository candidateRepository,
                               DepartmentRepository departmentRepository,
                               EmployeeRepository employeeRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               Cloudinary cloudinary,
                               NotificationService notificationService) {
        this.jobPostingRepository = jobPostingRepository;
        this.candidateRepository  = candidateRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository   = employeeRepository;
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.cloudinary           = cloudinary;
        this.notificationService  = notificationService;
    }

    @Value("${app.recruitment.screening-threshold:40}")
    private double screeningThreshold;

    // ===== JOB POSTINGS =====

    public JobPosting createJob(JobPosting job) {
        return jobPostingRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<JobPosting> getAllJobs() {
        return jobPostingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public JobPosting getJobById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Job posting not found"));
    }

    public JobPosting updateJobStatus(Long id, JobPosting.JobStatus status) {
        JobPosting job = getJobById(id);
        job.setStatus(status);
        return jobPostingRepository.save(job);
    }

    // ===== CANDIDATES =====

    public Candidate addCandidate(Long jobId, Candidate candidate, MultipartFile resumeFile) {
        JobPosting job = getJobById(jobId);
        candidate.setJobPosting(job);

        if (resumeFile != null && !resumeFile.isEmpty()) {
            byte[] fileBytes;
            try {
                fileBytes = resumeFile.getBytes();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read resume file: " + e.getMessage());
            }

            // Extract text now — before uploading — so screening never needs to download the file
            String extractedText = extractResumeText(fileBytes);
            candidate.setResumeText(extractedText);

            // Upload to Cloudinary for storage / viewing
            String resumeUrl = uploadResumeBytes(fileBytes, candidate.getName());
            candidate.setResumeUrl(resumeUrl);
        }

        Candidate saved = candidateRepository.save(candidate);

        // Notify candidate that application was received
        notificationService.notifyApplicationReceived(saved);

        // Auto-trigger AI screening AFTER the transaction commits
        // (firing inside the tx causes "Candidate not found" race condition)
        if (saved.getResumeUrl() != null) {
            final Long candidateId = saved.getId();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    screeningTriggerService.screenAsync(candidateId);
                }
            });
        }

        return saved;
    }

    private String extractResumeText(byte[] pdfBytes) {
        try {
            PDDocument doc = Loader.loadPDF(pdfBytes);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            doc.close();
            if (text == null || text.isBlank()) {
                return "(Resume appears to be a scanned image — no text could be extracted)";
            }
            // Cap at 6000 chars to stay within LLM context window
            return text.length() > 6000 ? text.substring(0, 6000) + "\n...[truncated]" : text;
        } catch (Exception e) {
            log.warn("Could not extract text from resume PDF: {}", e.getMessage());
            return "(Could not extract text from resume)";
        }
    }

    private String uploadResumeBytes(byte[] fileBytes, String candidateName) {
        try {
            String publicId = "hrms/resumes/" + candidateName.replaceAll("\\s+", "_")
                    + "_" + System.currentTimeMillis();
            Map uploadResult = cloudinary.uploader().upload(
                    fileBytes,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "raw",
                            "format", "pdf"
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload resume: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Candidate> getCandidatesForJob(Long jobId) {
        return candidateRepository.findByJobPostingIdOrderByAiScoreDesc(jobId);
    }

    /**
     * Updates pipeline stage and fires the appropriate email notification.
     * Also handles auto-advance/reject after screening based on threshold.
     */
    public Candidate updateCandidateStage(Long candidateId, Candidate.PipelineStage stage) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Candidate.PipelineStage previous = candidate.getPipelineStage();
        candidate.setPipelineStage(stage);
        Candidate saved = candidateRepository.save(candidate);

        // Fire notification for the new stage
        sendStageNotification(saved, stage);
        log.info("Candidate {} moved from {} to {}", candidateId, previous, stage);

        return saved;
    }

    private void sendStageNotification(Candidate candidate, Candidate.PipelineStage stage) {
        switch (stage) {
            case SCREENED  -> notificationService.notifyScreeningPassed(candidate);
            case INTERVIEW -> {
                if (candidate.getScheduledInterviewAt() != null) {
                    notificationService.notifyInterviewScheduled(candidate);
                }
            }
            case OFFER     -> notificationService.notifyOfferExtended(candidate);
            case REJECTED  -> notificationService.notifyRejected(candidate);
            default        -> { /* APPLIED & HIRED handled separately */ }
        }
    }

    /**
     * Called by AI screening service after Gemini scores the resume.
     * Auto-advances above threshold, auto-rejects below.
     */
    public Candidate updateAiScreeningResult(Long candidateId, Double score,
                                              Double skillMatch, String summary,
                                              String strengths, String gaps,
                                              String recommendation, Integer yearsExperience) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        candidate.setAiScore(BigDecimal.valueOf(score));
        candidate.setSkillMatchPercent(BigDecimal.valueOf(skillMatch));
        candidate.setAiSummary(summary);
        candidate.setAiStrengths(strengths);
        candidate.setAiGaps(gaps);
        candidate.setAiRecommendation(recommendation);

        int requiredYears = candidate.getJobPosting().getExperienceYears();
        int extractedYears = yearsExperience != null ? yearsExperience : 0;
        boolean skillsPass = skillMatch >= screeningThreshold;
        boolean experiencePass = extractedYears >= requiredYears;

        log.info("[SCREENING] Gate check — skillMatch={} (need ≥{}): {}, years={} (need ≥{}): {}",
                skillMatch, screeningThreshold, skillsPass,
                extractedYears, requiredYears, experiencePass);

        if (skillsPass && experiencePass) {
            candidate.setPipelineStage(Candidate.PipelineStage.SCREENED);
            Candidate saved = candidateRepository.save(candidate);
            notificationService.notifyScreeningPassed(saved);
            return saved;
        } else {
            candidate.setPipelineStage(Candidate.PipelineStage.REJECTED);
            Candidate saved = candidateRepository.save(candidate);
            notificationService.notifyScreeningRejected(saved);
            return saved;
        }
    }

    /**
     * HR schedules an interview for a candidate.
     * Sets the date/time, moves stage to INTERVIEW, and emails the candidate.
     */
    public Candidate scheduleInterview(Long candidateId, LocalDateTime scheduledAt, String meetingLink) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        candidate.setScheduledInterviewAt(scheduledAt);
        candidate.setInterviewMeetingLink(meetingLink);
        candidate.setPipelineStage(Candidate.PipelineStage.INTERVIEW);

        Candidate saved = candidateRepository.save(candidate);
        notificationService.notifyInterviewScheduled(saved);

        log.info("Interview scheduled for candidate {} at {}", candidateId, scheduledAt);
        return saved;
    }

    public Employee hireCandidate(Long candidateId, Long departmentId, Long managerId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Department department = departmentId != null ?
                departmentRepository.findById(departmentId).orElse(null) : null;

        Employee manager = managerId != null ?
                employeeRepository.findById(managerId).orElse(null) : null;

        Employee employee = Employee.builder()
                .firstName(candidate.getName().split(" ")[0])
                .lastName(candidate.getName().contains(" ") ?
                        candidate.getName().substring(candidate.getName().indexOf(" ") + 1) : "")
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .designation(candidate.getJobPosting().getTitle())
                .department(department)
                .manager(manager)
                .joinDate(LocalDate.now())
                .salary(java.math.BigDecimal.valueOf(50000))
                .build();

        employee = employeeRepository.save(employee);

        User user = User.builder()
                .email(candidate.getEmail())
                .passwordHash(passwordEncoder.encode("hrms@123"))
                .role(Role.ROLE_EMPLOYEE)
                .employee(employee)
                .build();
        userRepository.save(user);

        candidate.setPipelineStage(Candidate.PipelineStage.HIRED);
        candidateRepository.save(candidate);

        return employee;
    }

    @Transactional(readOnly = true)
    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));
    }

    // ===== OFFER LETTER =====

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public Candidate sendOfferLetter(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        String token = UUID.randomUUID().toString();
        candidate.setOfferToken(token);
        candidate.setOfferSentAt(LocalDateTime.now());
        candidate.setOfferStatus(Candidate.OfferStatus.PENDING);
        candidate.setPipelineStage(Candidate.PipelineStage.OFFER);

        Candidate saved = candidateRepository.save(candidate);

        String acceptUrl  = frontendUrl + "/offer?token=" + token + "&action=accept";
        String declineUrl = frontendUrl + "/offer?token=" + token + "&action=decline";
        notificationService.notifyOfferLetter(saved, acceptUrl, declineUrl);

        log.info("Offer letter sent to {} (token: {})", candidate.getEmail(), token);
        return saved;
    }

    public Candidate respondToOffer(String token, boolean accepted) {
        Candidate candidate = candidateRepository.findByOfferToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid offer token"));

        if (accepted) {
            candidate.setOfferStatus(Candidate.OfferStatus.ACCEPTED);
            candidate.setPipelineStage(Candidate.PipelineStage.HIRED);

            // Create employee account
            hireCandidate(candidate.getId(),
                    candidate.getJobPosting().getDepartment() != null
                            ? candidate.getJobPosting().getDepartment().getId() : null,
                    null);

            // Close the job posting
            JobPosting job = candidate.getJobPosting();
            job.setStatus(JobPosting.JobStatus.CLOSED);
            jobPostingRepository.save(job);

            notificationService.notifyOfferAccepted(candidate);
            log.info("Offer accepted by {} — job {} closed", candidate.getEmail(), job.getTitle());
        } else {
            candidate.setOfferStatus(Candidate.OfferStatus.DECLINED);
            candidate.setPipelineStage(Candidate.PipelineStage.REJECTED);
            notificationService.notifyOfferDeclined(candidate);
            log.info("Offer declined by {}", candidate.getEmail());
        }

        return candidateRepository.save(candidate);
    }

    @Transactional(readOnly = true)
    public Candidate getByOfferToken(String token) {
        return candidateRepository.findByOfferToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid offer token"));
    }
}
