package com.PreetishRamola.hrms.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScreeningResult {
    private Double score;

    @JsonProperty("skill_match")
    private Double skillMatch;

    private List<String> strengths;
    private List<String> gaps;
    private String summary;
    private String recommendation;   // STRONG_YES, YES, MAYBE, NO

    @JsonProperty("extracted_skills")
    private List<String> extractedSkills;

    @JsonProperty("years_experience")
    private Integer yearsExperience;
}
