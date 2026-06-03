package com.PreetishRamola.hrms.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Thin async wrapper so RecruitmentService can fire AI screening
 * without creating a circular dependency with ResumeScreeningService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScreeningTriggerService {

    private final ResumeScreeningService resumeScreeningService;

    @Async
    public void screenAsync(Long candidateId) {
        try {
            log.info("Auto-screening candidate {} after application", candidateId);
            resumeScreeningService.screenResume(candidateId);
        } catch (Exception e) {
            log.error("[SCREENING] Auto-screening failed for candidate {}: {}", candidateId, e.getMessage(), e);
            // Non-fatal — candidate stays in APPLIED stage; HR can retry manually
        }
    }
}
