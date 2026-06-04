package com.PreetishRamola.hrms.notification;

import com.PreetishRamola.hrms.recruitment.Candidate;
import com.PreetishRamola.hrms.recruitment.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewReminderScheduler {

    private final CandidateRepository candidateRepository;
    private final NotificationService notificationService;

    /**
     * Runs every 30 minutes.
     * Finds candidates whose interview is between 23h and 25h from now (24h reminder window)
     * and whose stage is INTERVIEW — sends one reminder email.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void sendInterviewReminders() {
        LocalDateTime from = LocalDateTime.now().plusHours(23);
        LocalDateTime to   = LocalDateTime.now().plusHours(25);

        List<Candidate> upcoming = candidateRepository
                .findByPipelineStageAndScheduledInterviewAtBetween(
                        Candidate.PipelineStage.INTERVIEW, from, to);

        for (Candidate c : upcoming) {
            log.info("Sending 24h reminder to {} for interview at {}", c.getEmail(), c.getScheduledInterviewAt());
            notificationService.notifyInterviewReminder(c);
        }
    }
}
