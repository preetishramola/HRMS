package com.PreetishRamola.hrms.ai;

import com.PreetishRamola.hrms.attendance.AttendanceRepository;
import com.PreetishRamola.hrms.employee.Role;
import com.PreetishRamola.hrms.employee.User;
import com.PreetishRamola.hrms.employee.UserRepository;
import com.PreetishRamola.hrms.leave.LeaveRepository;
import com.PreetishRamola.hrms.leave.LeaveRequest;
import com.PreetishRamola.hrms.payroll.PayrollRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserRepository userRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;
    private final PayrollRepository payrollRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CHAT_SESSION_PREFIX = "chatbot:session:";
    private static final Duration SESSION_TTL = Duration.ofHours(4);
    private static final int MAX_HISTORY_TURNS = 8;

    public ChatResponse askQuestion(String question, String userEmail, String sessionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Resolve or create session
        String resolvedSessionId = (sessionId != null && !sessionId.isBlank())
                ? sessionId : UUID.randomUUID().toString();
        String redisKey = CHAT_SESSION_PREFIX + resolvedSessionId;

        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = Optional.ofNullable(
                (List<Map<String, String>>) redisTemplate.opsForValue().get(redisKey)
        ).orElseGet(ArrayList::new);

        String context = buildContext(user);
        String fullPrompt = buildPrompt(context, question, history, user);

        String answer;
        try {
            answer = chatClientBuilder.build()
                    .prompt()
                    .user(fullPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Chatbot error for user {}: {}", userEmail, e.getMessage());
            answer = "I'm having trouble connecting right now. Please try again in a moment.";
        }

        // Update conversation history
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", question);
        history.add(userMsg);

        Map<String, String> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", answer);
        history.add(assistantMsg);

        // Trim to last MAX_HISTORY_TURNS exchanges
        while (history.size() > MAX_HISTORY_TURNS * 2) {
            history.remove(0);
            history.remove(0);
        }

        redisTemplate.opsForValue().set(redisKey, history, SESSION_TTL);

        return new ChatResponse(answer, resolvedSessionId);
    }

    private String buildContext(User user) {
        if (user.getEmployee() == null) {
            return buildAdminContext(user);
        }

        Long empId = user.getEmployee().getId();
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        long casualTaken = leaveRepository.countApprovedLeavesByType(empId, LeaveRequest.LeaveType.CASUAL, year);
        long sickTaken = leaveRepository.countApprovedLeavesByType(empId, LeaveRequest.LeaveType.SICK, year);
        long earnedTaken = leaveRepository.countApprovedLeavesByType(empId, LeaveRequest.LeaveType.EARNED, year);
        long presentDays = attendanceRepository.countPresentDays(empId, month, year);
        List<LeaveRequest> allLeaves = leaveRepository.findByEmployeeId(empId);

        var payslips = payrollRepository.findByEmployeeId(empId);
        String payslipInfo = payslips.isEmpty() ? "No payslips available yet"
                : "Latest payslip (" + payslips.get(0).getMonth() + "/" + payslips.get(0).getYear() + "): "
                + "Net Pay ₹" + payslips.get(0).getNetPay().toPlainString();

        // Build detailed leave history string
        StringBuilder leaveHistory = new StringBuilder();
        if (allLeaves.isEmpty()) {
            leaveHistory.append("  (No leave applications found)");
        } else {
            for (LeaveRequest lr : allLeaves) {
                leaveHistory.append("  - ").append(lr.getLeaveType().name())
                        .append(" leave | ").append(lr.getFromDate()).append(" to ").append(lr.getToDate())
                        .append(" | Status: ").append(lr.getStatus().name());
                if (lr.getReason() != null && !lr.getReason().isBlank()) {
                    leaveHistory.append(" | Reason: ").append(lr.getReason());
                }
                if (lr.getStatus() == LeaveRequest.LeaveStatus.REJECTED && lr.getRejectionReason() != null) {
                    leaveHistory.append(" | Rejection reason: ").append(lr.getRejectionReason());
                }
                leaveHistory.append("\n");
            }
        }

        return """
                EMPLOYEE PROFILE:
                Name: %s
                Designation: %s
                Department: %s
                Joined: %s

                LEAVE BALANCE (%d):
                - Casual: %d remaining (%d taken of 12)
                - Sick: %d remaining (%d taken of 7)
                - Earned: %d remaining (%d taken of 15)

                LEAVE APPLICATIONS (all time):
                %s
                ATTENDANCE THIS MONTH: %d days present

                PAYROLL: %s
                """.formatted(
                user.getEmployee().getFullName(),
                user.getEmployee().getDesignation() != null ? user.getEmployee().getDesignation() : "N/A",
                user.getEmployee().getDepartment() != null ? user.getEmployee().getDepartment().getName() : "N/A",
                user.getEmployee().getJoinDate(),
                year,
                Math.max(0, 12 - casualTaken), casualTaken,
                Math.max(0, 7 - sickTaken), sickTaken,
                Math.max(0, 15 - earnedTaken), earnedTaken,
                leaveHistory.toString(),
                presentDays,
                payslipInfo
        );
    }

    private String buildAdminContext(User user) {
        return "USER ROLE: " + user.getRole().name() + "\n"
                + "Note: This user does not have a linked employee record.\n"
                + "You can help with general HR policies and system navigation.";
    }

    private String buildPrompt(String context, String question,
                               List<Map<String, String>> history, User user) {
        String roleName = switch (user.getRole()) {
            case ROLE_ADMIN -> "Admin";
            case ROLE_MANAGER -> "Manager";
            case ROLE_HR -> "HR Recruiter";
            default -> user.getEmployee() != null ? user.getEmployee().getFullName() : "User";
        };

        StringBuilder sb = new StringBuilder();
        sb.append("""
                You are a helpful, friendly HR Assistant for an AI-powered HRMS system.
                You are speaking with %s.

                THEIR HR DATA:
                %s

                HR POLICIES (standard):
                - Working hours: 9 AM - 6 PM, Monday to Friday
                - Leave year resets on January 1st
                - Leave applications need 24-hour advance notice for casual leave
                - Sick leave requires medical certificate for > 2 consecutive days
                - Payslips are generated on the last working day of each month

                CONVERSATION HISTORY:
                """.formatted(roleName, context));

        for (Map<String, String> msg : history) {
            sb.append("user".equals(msg.get("role")) ? roleName + ": " : "Assistant: ");
            sb.append(msg.get("content")).append("\n");
        }

        sb.append("\n").append(roleName).append(": ").append(question);
        sb.append("""

                \nAssistant: (Respond concisely and helpfully. Use bullet points for lists.
                If the data doesn't contain the answer, say so politely. Never make up numbers.)
                """);

        return sb.toString();
    }

    public record ChatResponse(String message, String sessionId) {}
}
