package com.PreetishRamola.hrms.leave.dto;

import com.PreetishRamola.hrms.leave.LeaveRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDto {
    @NotNull private LeaveRequest.LeaveType leaveType;
    @NotNull private LocalDate fromDate;
    @NotNull private LocalDate toDate;
    private String reason;
}
