package com.PreetishRamola.hrms.feedback;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotNull(message = "Recipient employee ID is required")
    private Long toEmployeeId;

    @NotNull(message = "Category is required")
    private Feedback.FeedbackCategory category;

    @NotBlank(message = "Feedback content cannot be empty")
    @Size(min = 10, max = 2000, message = "Feedback must be between 10 and 2000 characters")
    private String content;
}
