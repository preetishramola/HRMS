package com.PreetishRamola.hrms.complaint;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComplaintStatusRequest {

    @NotNull
    private Complaint.ComplaintStatus status;

    private String hrNotes;
}
