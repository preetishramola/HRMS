package com.PreetishRamola.hrms.ai;

import com.PreetishRamola.hrms.recruitment.Candidate;
import com.PreetishRamola.hrms.recruitment.CandidateRepository;
import com.PreetishRamola.hrms.recruitment.JobPosting;
import com.PreetishRamola.hrms.recruitment.RecruitmentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeScreeningService {

    private final CandidateRepository candidateRepository;
    private final RecruitmentService recruitmentService;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String groqApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String groqBaseUrl;

    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    @Transactional
    public ScreeningResult screenResume(Long candidateId) {
        log.info("[SCREENING] ▶ Starting for candidate {}", candidateId);

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found: " + candidateId));

        if (candidate.getResumeUrl() == null) {
            throw new IllegalStateException("No resume uploaded for candidate " + candidateId);
        }

        JobPosting job = candidate.getJobPosting();
        log.info("[SCREENING] Candidate='{}', Job='{}'", candidate.getName(), job.getTitle());

        // ── Step 1: Get resume text (extracted at upload time, stored in DB) ──
        String resumeText = candidate.getResumeText();
        if (resumeText == null || resumeText.isBlank()) {
            log.warn("[SCREENING] resumeText is empty for candidate {} — resume may have been uploaded before this feature was added", candidateId);
            resumeText = "(Resume text not available — candidate should re-upload their resume)";
        } else {
            log.info("[SCREENING] Step 1 ✓ Resume text loaded from DB: {} chars", resumeText.length());
        }

        // ── Step 2: Call Groq ────────────────────────────────────────────────
        String rawResponse;
        try {
            log.info("[SCREENING] Step 2 – Calling Groq ({})", GROQ_MODEL);
            String prompt = buildScreeningPrompt(candidate, job, resumeText);
            rawResponse = callGroq(prompt);
            log.info("[SCREENING] Step 2 ✓ Groq responded ({} chars)", rawResponse.length());
            log.debug("[SCREENING] Groq raw: {}", rawResponse);
        } catch (HttpClientErrorException e) {
            log.error("[SCREENING] Step 2 ✗ Groq HTTP {} – {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Groq API error " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("[SCREENING] Step 2 ✗ Groq call failed: {}", e.getMessage());
            throw new RuntimeException("Groq call failed: " + e.getMessage(), e);
        }

        // ── Step 3: Parse + persist ──────────────────────────────────────────
        ScreeningResult result;
        try {
            log.info("[SCREENING] Step 3 – Parsing JSON response");
            result = parseScreeningResponse(rawResponse);
            log.info("[SCREENING] Step 3 ✓ score={}, skillMatch={}, recommendation={}",
                    result.getScore(), result.getSkillMatch(), result.getRecommendation());
        } catch (Exception e) {
            log.error("[SCREENING] Step 3 ✗ JSON parse failed. Raw: {}", rawResponse);
            throw new RuntimeException("AI returned unparseable JSON: " + e.getMessage(), e);
        }

        persistScreeningResult(candidate, result);
        log.info("[SCREENING] ✅ Complete for candidate {} – {} (score={}, skillMatch={}, rec={})",
                candidateId, candidate.getName(), result.getScore(), result.getSkillMatch(), result.getRecommendation());
        return result;
    }

    private String buildScreeningPrompt(Candidate candidate, JobPosting job, String resumeText) {
        return """
                You are a senior technical recruiter. Analyze this resume for the given job.

                JOB:
                Title: %s
                Required Experience: %s+ years
                Required Skills: %s
                Description: %s

                CANDIDATE: %s

                RESUME TEXT:
                ---
                %s
                ---

                Respond ONLY with this exact JSON (no markdown, no extra text):
                {
                  "score": <0-100>,
                  "skill_match": <0-100>,
                  "years_experience": <integer>,
                  "extracted_skills": ["skill1", "skill2"],
                  "strengths": ["strength1", "strength2"],
                  "gaps": ["gap1"],
                  "summary": "<2-3 sentence assessment>",
                  "recommendation": "<STRONG_YES|YES|MAYBE|NO>"
                }
                """.formatted(
                job.getTitle(),
                job.getExperienceYears(),
                String.join(", ", job.getRequiredSkills()),
                job.getDescription(),
                candidate.getName(),
                resumeText
        );
    }

    private String callGroq(String prompt) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        RestTemplate restTemplate = new RestTemplate(factory);

        String url = groqBaseUrl + "/v1/chat/completions";
        Map<String, Object> requestBody = Map.of(
                "model", GROQ_MODEL,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.1,
                "max_tokens", 1500
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        ResponseEntity<String> response = restTemplate.exchange(
                URI.create(url), HttpMethod.POST,
                new HttpEntity<>(requestBody, headers), String.class);

        return extractTextFromGroqResponse(response.getBody());
    }

    private String extractTextFromGroqResponse(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected Groq response shape: " + body);
        }
    }

    private ScreeningResult parseScreeningResponse(String raw) {
        String json = raw.trim()
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("```", "")
                .trim();
        try {
            return objectMapper.readValue(json, ScreeningResult.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON parse error: " + e.getMessage());
        }
    }

    private void persistScreeningResult(Candidate candidate, ScreeningResult result) {
        try {
            String strengthsJson = result.getStrengths() != null
                    ? objectMapper.writeValueAsString(result.getStrengths()) : null;
            String gapsJson = result.getGaps() != null
                    ? objectMapper.writeValueAsString(result.getGaps()) : null;
            double score      = result.getScore()      != null ? result.getScore()      : 0.0;
            double skillMatch = result.getSkillMatch() != null ? result.getSkillMatch() : 0.0;

            recruitmentService.updateAiScreeningResult(
                    candidate.getId(), score, skillMatch,
                    result.getSummary(), strengthsJson, gapsJson,
                    result.getRecommendation(), result.getYearsExperience());
        } catch (Exception e) {
            log.error("[SCREENING] Failed to persist result for candidate {}: {}", candidate.getId(), e.getMessage(), e);
        }
    }
}
