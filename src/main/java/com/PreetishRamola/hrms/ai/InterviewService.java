package com.PreetishRamola.hrms.ai;

import com.PreetishRamola.hrms.recruitment.Candidate;
import com.PreetishRamola.hrms.recruitment.CandidateRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CandidateRepository candidateRepository;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private static final String SESSION_PREFIX = "interview:session:";
    private static final Duration SESSION_TTL = Duration.ofHours(2);
    private static final int MAX_QUESTIONS = 6;

    public InterviewSession startInterview(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        String sessionId = UUID.randomUUID().toString();

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("candidateId", candidateId.toString());
        sessionData.put("candidateName", candidate.getName());
        sessionData.put("jobTitle", candidate.getJobPosting().getTitle());
        sessionData.put("requiredSkills", String.join(", ", candidate.getJobPosting().getRequiredSkills()));
        sessionData.put("questionCount", 0);
        sessionData.put("history", new ArrayList<Map<String, String>>());

        // Feed resume screening data into interview context
        sessionData.put("resumeSummary", candidate.getAiSummary() != null ? candidate.getAiSummary() : "No resume summary available");
        sessionData.put("resumeStrengths", candidate.getAiStrengths() != null ? candidate.getAiStrengths() : "[]");
        sessionData.put("resumeGaps", candidate.getAiGaps() != null ? candidate.getAiGaps() : "[]");
        sessionData.put("skillMatchPercent", candidate.getSkillMatchPercent() != null ? candidate.getSkillMatchPercent().toString() : "N/A");
        sessionData.put("yearsExperience", "N/A");

        String firstQuestion = generateFirstQuestion(candidate);
        addToHistory(sessionData, "assistant", firstQuestion);
        sessionData.put("questionCount", 1);

        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, sessionData, SESSION_TTL);

        return InterviewSession.builder()
                .sessionId(sessionId)
                .question(firstQuestion)
                .questionNumber(1)
                .totalQuestions(MAX_QUESTIONS)
                .candidateName(candidate.getName())
                .jobTitle(candidate.getJobPosting().getTitle())
                .complete(false)
                .build();
    }

    @SuppressWarnings("unchecked")
    public InterviewSession respond(String sessionId, String answer) {
        String redisKey = SESSION_PREFIX + sessionId;
        Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(redisKey);

        if (sessionData == null) {
            throw new IllegalStateException("Interview session not found or expired");
        }

        addToHistory(sessionData, "user", answer);
        int questionCount = ((Number) sessionData.get("questionCount")).intValue();

        if (questionCount >= MAX_QUESTIONS) {
            redisTemplate.opsForValue().set(redisKey, sessionData, SESSION_TTL);
            return InterviewSession.builder()
                    .sessionId(sessionId)
                    .question("Thank you for completing the interview. I have all the information I need.")
                    .questionNumber(questionCount)
                    .totalQuestions(MAX_QUESTIONS)
                    .complete(true)
                    .build();
        }

        String nextQuestion = generateNextQuestion(sessionData, answer, questionCount);
        addToHistory(sessionData, "assistant", nextQuestion);
        sessionData.put("questionCount", questionCount + 1);
        redisTemplate.opsForValue().set(redisKey, sessionData, SESSION_TTL);

        return InterviewSession.builder()
                .sessionId(sessionId)
                .question(nextQuestion)
                .questionNumber(questionCount + 1)
                .totalQuestions(MAX_QUESTIONS)
                .complete(false)
                .build();
    }

    @SuppressWarnings("unchecked")
    public InterviewSession endInterview(String sessionId) {
        String redisKey = SESSION_PREFIX + sessionId;
        Map<String, Object> sessionData = (Map<String, Object>) redisTemplate.opsForValue().get(redisKey);

        if (sessionData == null) {
            throw new IllegalStateException("Session not found or already ended");
        }

        InterviewSession.InterviewEvaluation evaluation = generateEvaluation(sessionData);
        String reportText = formatReportText(sessionData, evaluation);

        Long candidateId = Long.valueOf(sessionData.get("candidateId").toString());
        candidateRepository.findById(candidateId).ifPresent(c -> {
            c.setInterviewReport(reportText);
            c.setInterviewCommScore(evaluation.getCommunication());
            c.setInterviewTechScore(evaluation.getTechnical());
            c.setInterviewProblemScore(evaluation.getProblemSolving());
            c.setInterviewRecommendation(evaluation.getRecommendation());
            if ("HIRE".equals(evaluation.getRecommendation())) {
                c.setPipelineStage(Candidate.PipelineStage.OFFER);
            } else {
                c.setPipelineStage(Candidate.PipelineStage.INTERVIEW);
            }
            candidateRepository.save(c);
        });

        redisTemplate.delete(redisKey);

        return InterviewSession.builder()
                .sessionId(sessionId)
                .complete(true)
                .evaluation(evaluation)
                .candidateName(sessionData.get("candidateName").toString())
                .jobTitle(sessionData.get("jobTitle").toString())
                .build();
    }

    @SuppressWarnings("unchecked")
    private void addToHistory(Map<String, Object> sessionData, String role, String content) {
        List<Map<String, String>> history = (List<Map<String, String>>) sessionData.get("history");
        Map<String, String> entry = new HashMap<>();
        entry.put("role", role);
        entry.put("content", content);
        history.add(entry);
    }

    private String generateFirstQuestion(Candidate candidate) {
        String resumeContext = candidate.getAiSummary() != null
                ? "Resume summary: " + candidate.getAiSummary()
                : "No resume on file.";
        String strengths = candidate.getAiStrengths() != null ? candidate.getAiStrengths() : "[]";
        String gaps = candidate.getAiGaps() != null ? candidate.getAiGaps() : "[]";

        String prompt = """
                You are a sharp, professional technical interviewer at a top company.
                Position: %s
                Candidate name: %s
                Required skills: %s

                RESUME INTELLIGENCE (from AI screening):
                %s
                Identified strengths: %s
                Identified gaps: %s

                Open the interview. Greet %s by name, then ask ONE specific opening question that references
                something concrete from their background. Make it personal — not generic.
                Keep it to 2-3 sentences. Do NOT ask multiple questions. Do NOT use bullet points.
                """.formatted(
                candidate.getJobPosting().getTitle(),
                candidate.getName(),
                String.join(", ", candidate.getJobPosting().getRequiredSkills()),
                resumeContext, strengths, gaps,
                candidate.getName().split(" ")[0]
        );
        return callGroq(prompt);
    }

    @SuppressWarnings("unchecked")
    private String generateNextQuestion(Map<String, Object> sessionData, String lastAnswer, int questionCount) {
        List<Map<String, String>> history = (List<Map<String, String>>) sessionData.get("history");
        String jobTitle = sessionData.get("jobTitle").toString();
        String requiredSkills = sessionData.get("requiredSkills").toString();

        StringBuilder transcript = new StringBuilder();
        for (Map<String, String> msg : history) {
            transcript.append("assistant".equals(msg.get("role")) ? "Interviewer: " : "Candidate: ");
            transcript.append(msg.get("content")).append("\n\n");
        }

        String resumeSummary = sessionData.getOrDefault("resumeSummary", "N/A").toString();
        String resumeGaps = sessionData.getOrDefault("resumeGaps", "[]").toString();
        String skillMatch = sessionData.getOrDefault("skillMatchPercent", "N/A").toString();

        String questionType = switch (questionCount) {
            case 1 -> "Ask a deep technical question specifically probing a skill from: " + requiredSkills
                    + ". If the resume shows gaps in any of these, target those gaps directly.";
            case 2 -> "Ask a behavioral question (STAR format) about a real challenge they faced. "
                    + "Reference something specific from their background: " + resumeSummary;
            case 3 -> "The resume shows these potential gaps: " + resumeGaps + ". "
                    + "Ask a pointed question to probe whether these gaps are real. Be direct but professional.";
            case 4 -> "Ask a system design or problem-solving scenario relevant to " + jobTitle
                    + ". Make it a real-world scenario they'd face in this role.";
            case 5 -> "This is the final question. Ask about a specific project or achievement from their background, "
                    + "and what measurable impact it had. Then close the interview professionally.";
            default -> "Based on their answers so far, ask a sharp follow-up that tests the depth of a claim they made.";
        };

        String prompt = """
                You are a senior technical interviewer for a %s role. Candidate: %s.

                RESUME INTELLIGENCE:
                Summary: %s
                Skill match: %s%%
                Identified gaps: %s

                CONVERSATION SO FAR:
                %s

                YOUR TASK: %s

                STRICT RULES:
                - Exactly ONE question, maximum 2 sentences
                - Make it specific and personal to THIS candidate, not generic
                - Do NOT repeat any question already asked
                - Do NOT use bullet points or numbered lists
                - If they gave a vague answer, call it out and ask for specifics
                """.formatted(
                jobTitle,
                sessionData.get("candidateName"),
                resumeSummary,
                skillMatch,
                resumeGaps,
                transcript,
                questionType
        );

        return callGroq(prompt);
    }

    @SuppressWarnings("unchecked")
    private InterviewSession.InterviewEvaluation generateEvaluation(Map<String, Object> sessionData) {
        List<Map<String, String>> history = (List<Map<String, String>>) sessionData.get("history");

        StringBuilder transcript = new StringBuilder();
        for (Map<String, String> msg : history) {
            transcript.append("assistant".equals(msg.get("role")) ? "Q: " : "A: ");
            transcript.append(msg.get("content")).append("\n\n");
        }

        String prompt = """
                You are a senior hiring manager. Evaluate this interview transcript objectively.

                Candidate: %s
                Position: %s
                Required skills: %s

                TRANSCRIPT:
                %s

                Provide a structured evaluation as a JSON object. Return ONLY valid JSON, no other text:
                {
                  "communication": <score 1-10>,
                  "technical": <score 1-10>,
                  "problem_solving": <score 1-10>,
                  "overall": <weighted average as decimal like 7.5>,
                  "recommendation": "<exactly one of: HIRE, CONSIDER, REJECT>",
                  "summary": "<3-4 sentence objective evaluation>",
                  "highlights": ["specific positive observation 1", "specific positive observation 2"],
                  "concerns": ["specific concern 1"]
                }

                Score rubric: 9-10 exceptional, 7-8 strong, 5-6 adequate, 3-4 weak, 1-2 poor.
                overall = (communication * 0.3) + (technical * 0.5) + (problem_solving * 0.2)
                HIRE if overall >= 7.0, CONSIDER if 5.0-6.9, REJECT if < 5.0
                """.formatted(
                sessionData.get("candidateName"),
                sessionData.get("jobTitle"),
                sessionData.get("requiredSkills"),
                transcript
        );

        String rawResponse = callGroq(prompt);

        try {
            String json = rawResponse.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("```", "")
                    .trim();
            return objectMapper.readValue(json, InterviewSession.InterviewEvaluation.class);
        } catch (Exception e) {
            log.error("Failed to parse evaluation JSON: {}", rawResponse);
            // Return a safe fallback structure rather than crashing
            return InterviewSession.InterviewEvaluation.builder()
                    .communication(6).technical(6).problemSolving(6)
                    .overall(6.0).recommendation("CONSIDER")
                    .summary("Evaluation parsing failed. Please review transcript manually.")
                    .highlights(List.of("Interview completed"))
                    .concerns(List.of("Automated evaluation unavailable"))
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private String formatReportText(Map<String, Object> sessionData,
                                    InterviewSession.InterviewEvaluation evaluation) {
        List<Map<String, String>> history = (List<Map<String, String>>) sessionData.get("history");
        StringBuilder sb = new StringBuilder();
        sb.append("INTERVIEW REPORT\n");
        sb.append("Candidate: ").append(sessionData.get("candidateName")).append("\n");
        sb.append("Position: ").append(sessionData.get("jobTitle")).append("\n\n");
        sb.append("SCORES\n");
        sb.append("Communication: ").append(evaluation.getCommunication()).append("/10\n");
        sb.append("Technical: ").append(evaluation.getTechnical()).append("/10\n");
        sb.append("Problem Solving: ").append(evaluation.getProblemSolving()).append("/10\n");
        sb.append("Overall: ").append(evaluation.getOverall()).append("/10\n");
        sb.append("Recommendation: ").append(evaluation.getRecommendation()).append("\n\n");
        sb.append("SUMMARY\n").append(evaluation.getSummary()).append("\n\n");
        sb.append("TRANSCRIPT\n");
        for (Map<String, String> msg : history) {
            sb.append("assistant".equals(msg.get("role")) ? "Q: " : "A: ");
            sb.append(msg.get("content")).append("\n\n");
        }
        return sb.toString();
    }

    private String callGroq(String prompt) {
        try {
            return chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Groq API call failed: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage());
        }
    }
}
