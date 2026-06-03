package com.PreetishRamola.hrms.recruitment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByJobPostingId(Long jobId);
    List<Candidate> findByJobPostingIdOrderByAiScoreDesc(Long jobId);
    List<Candidate> findByPipelineStage(Candidate.PipelineStage stage);

    @org.springframework.data.jpa.repository.Query("SELECT c.pipelineStage, COUNT(c) FROM Candidate c GROUP BY c.pipelineStage")
    List<Object[]> countByPipelineStage();

    @org.springframework.data.jpa.repository.Query("SELECT c.jobPosting.title, c.pipelineStage, COUNT(c) FROM Candidate c GROUP BY c.jobPosting.title, c.pipelineStage")
    List<Object[]> countByJobAndStage();

    @org.springframework.data.jpa.repository.Query("SELECT AVG(c.aiScore) FROM Candidate c WHERE c.aiScore IS NOT NULL")
    Double avgAiScore();

    List<Candidate> findByPipelineStageAndScheduledInterviewAtBetween(
            Candidate.PipelineStage stage, LocalDateTime from, LocalDateTime to);

    java.util.Optional<Candidate> findByOfferToken(String token);
}
