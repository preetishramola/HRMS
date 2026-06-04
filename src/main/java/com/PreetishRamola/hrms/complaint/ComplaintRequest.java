package com.PreetishRamola.hrms.complaint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplaintRequest {

    @NotNull(message = "Category is required")
    private Complaint.ComplaintCategory category;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 20, max = 5000, message = "Description must be between 20 and 5000 characters")
    private String description;
}
