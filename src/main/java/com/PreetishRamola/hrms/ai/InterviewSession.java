package com.PreetishRamola.hrms.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class InterviewSession {
    private String sessionId;
    private String question;
    private int questionNumber;
    private int totalQuestions;
    private String candidateName;
    private String jobTitle;

    @JsonProperty("isComplete")
    @Builder.Default
    private boolean complete = false;

    // Populated only when isComplete = true (from endInterview)
    private InterviewEvaluation evaluation;

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InterviewEvaluation {
        private Integer communication;
        private Integer technical;
        @JsonProperty("problem_solving")
        private Integer problemSolving;
        private Double overall;
        private String recommendation;   // HIRE, CONSIDER, REJECT
        private String summary;
        private List<String> highlights;
        private List<String> concerns;
    }
}
