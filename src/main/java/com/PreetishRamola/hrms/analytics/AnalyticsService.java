package com.PreetishRamola.hrms.analytics;

import com.PreetishRamola.hrms.attendance.AttendanceRepository;
import com.PreetishRamola.hrms.department.DepartmentRepository;
import com.PreetishRamola.hrms.employee.Employee;
import com.PreetishRamola.hrms.employee.EmployeeRepository;
import com.PreetishRamola.hrms.leave.LeaveRepository;
import com.PreetishRamola.hrms.leave.LeaveRequest;
import com.PreetishRamola.hrms.payroll.PayrollRepository;
import com.PreetishRamola.hrms.performance.PerformanceRepository;
import com.PreetishRamola.hrms.recruitment.Candidate;
import com.PreetishRamola.hrms.recruitment.CandidateRepository;
import com.PreetishRamola.hrms.recruitment.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final PerformanceRepository performanceRepository;
    private final CandidateRepository candidateRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ChatClient.Builder chatClientBuilder;

    // ── Overview ──────────────────────────────────────────────────────────────

    public Map<String, Object> getOverview() {
        int month = LocalDate.now().getMonthValue();
        int year  = LocalDate.now().getYear();

        long total   = employeeRepository.count();
        long active  = employeeRepository.countByStatus(Employee.EmploymentStatus.ACTIVE);
        long depts   = departmentRepository.count();
        long openJobs = jobPostingRepository.countByStatus(
                com.PreetishRamola.hrms.recruitment.JobPosting.JobStatus.OPEN);
        long pending  = leaveRepository.countPending();

        Double payrollCost = payrollRepository.sumNetPayByMonthAndYear(month, year);

        long totalCandidates = candidateRepository.count();
        long hired = candidateRepository.findByPipelineStage(Candidate.PipelineStage.HIRED).size();

        return Map.of(
                "totalEmployees", total,
                "activeEmployees", active,
                "inactiveEmployees", total - active,
                "totalDepartments", depts,
                "openPositions", openJobs,
                "pendingLeaves", pending,
                "monthlyPayrollCost", payrollCost != null ? payrollCost : 0.0,
                "totalCandidates", totalCandidates,
                "totalHired", hired
        );
    }

    // ── Hiring Funnel ─────────────────────────────────────────────────────────

    public Map<String, Object> getHiringFunnel() {
        List<Object[]> stageCounts = candidateRepository.countByPipelineStage();
        Map<String, Long> funnel = new LinkedHashMap<>();
        // Initialise all stages in order
        for (Candidate.PipelineStage s : Candidate.PipelineStage.values()) {
            funnel.put(s.name(), 0L);
        }
        for (Object[] row : stageCounts) {
            funnel.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        List<Object[]> byJob = candidateRepository.countByJobAndStage();
        Map<String, Map<String, Long>> perJob = new LinkedHashMap<>();
        for (Object[] row : byJob) {
            String jobTitle = row[0].toString();
            String stage    = row[1].toString();
            long count      = ((Number) row[2]).longValue();
            perJob.computeIfAbsent(jobTitle, k -> new LinkedHashMap<>()).put(stage, count);
        }

        Double avgScore = candidateRepository.avgAiScore();

        return Map.of(
                "pipeline", funnel,
                "byJob", perJob,
                "avgAiScore", avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0,
                "openJobs", jobPostingRepository.findAll().stream()
                        .filter(j -> j.getStatus() == com.PreetishRamola.hrms.recruitment.JobPosting.JobStatus.OPEN)
                        .map(j -> Map.of("id", j.getId(), "title", j.getTitle(),
                                "department", j.getDepartment() != null ? j.getDepartment().getName() : "—",
                                "experience", j.getExperienceYears() != null ? j.getExperienceYears() : 0))
                        .collect(Collectors.toList())
        );
    }

    // ── Department Stats ──────────────────────────────────────────────────────

    public List<Map<String, Object>> getDepartmentStats() {
        int year = LocalDate.now().getYear();
        return departmentRepository.findAll().stream().map(dept -> {
            long headcount = employeeRepository.countByDepartmentId(dept.getId());
            Double avgRating = performanceRepository.avgRatingByDepartment(dept.getId(), year);

            List<Employee> employees = employeeRepository.findByDepartmentId(dept.getId());
            OptionalDouble avgSalary = employees.stream()
                    .mapToDouble(e -> e.getSalary() != null ? e.getSalary().doubleValue() : 0)
                    .average();

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", dept.getId());
            row.put("name", dept.getName());
            row.put("headcount", headcount);
            row.put("avgSalary", avgSalary.isPresent()
                    ? Math.round(avgSalary.getAsDouble()) : 0);
            row.put("avgRating", avgRating != null
                    ? Math.round(avgRating * 10.0) / 10.0 : null);
            return row;
        }).collect(Collectors.toList());
    }

    // ── Attendance Trends ─────────────────────────────────────────────────────

    public Map<String, Object> getAttendanceTrends() {
        LocalDate today = LocalDate.now();
        LocalDate from  = today.minusDays(29);
        long totalActive = employeeRepository.countByStatus(Employee.EmploymentStatus.ACTIVE);

        List<Object[]> rows = attendanceRepository.countPresentByDateRange(from, today);
        List<Map<String, Object>> daily = rows.stream().map(r -> {
            long present = ((Number) r[1]).longValue();
            double rate  = totalActive > 0
                    ? Math.round((present * 100.0 / totalActive) * 10.0) / 10.0 : 0;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("date", r[0].toString());
            entry.put("present", present);
            entry.put("rate", rate);
            return entry;
        }).collect(Collectors.toList());

        // This month's overall
        int month = today.getMonthValue();
        int year  = today.getYear();
        long presentThisMonth = attendanceRepository.countTotalPresentDays(month, year);
        long workingDaysElapsed = (long) today.getDayOfMonth();
        double monthlyRate = (totalActive > 0 && workingDaysElapsed > 0)
                ? Math.round((presentThisMonth * 100.0 / (totalActive * workingDaysElapsed)) * 10.0) / 10.0
                : 0;

        return Map.of(
                "daily", daily,
                "monthlyRate", monthlyRate,
                "totalActiveEmployees", totalActive
        );
    }

    // ── Leave Stats ───────────────────────────────────────────────────────────

    public Map<String, Object> getLeaveStats() {
        int year = LocalDate.now().getYear();

        List<Object[]> byType = leaveRepository.countApprovedByTypeForYear(year);
        Map<String, Long> leaveByType = new LinkedHashMap<>();
        for (LeaveRequest.LeaveType t : LeaveRequest.LeaveType.values()) {
            leaveByType.put(t.name(), 0L);
        }
        for (Object[] row : byType) {
            leaveByType.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        List<Object[]> byDept = leaveRepository.countApprovedByDepartment(year);
        Map<String, Long> leaveByDept = new LinkedHashMap<>();
        for (Object[] row : byDept) {
            if (row[0] != null) leaveByDept.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        return Map.of(
                "byType", leaveByType,
                "byDepartment", leaveByDept,
                "pendingCount", leaveRepository.countPending(),
                "year", year
        );
    }

    // ── Performance Overview ──────────────────────────────────────────────────

    public Map<String, Object> getPerformanceOverview() {
        int year = LocalDate.now().getYear();
        List<Map<String, Object>> deptPerf = departmentRepository.findAll().stream()
                .map(dept -> {
                    Double avg = performanceRepository.avgRatingByDepartment(dept.getId(), year);
                    return Map.<String, Object>of(
                            "department", dept.getName(),
                            "avgRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0
                    );
                })
                .filter(m -> (Double) m.get("avgRating") > 0)
                .collect(Collectors.toList());

        long reviews = performanceRepository.count();
        OptionalDouble overall = performanceRepository.findAll().stream()
                .mapToDouble(p -> p.getRating() != null ? p.getRating().doubleValue() : 0)
                .average();

        return Map.of(
                "byDepartment", deptPerf,
                "totalReviews", reviews,
                "overallAvgRating", overall.isPresent()
                        ? Math.round(overall.getAsDouble() * 10.0) / 10.0 : 0.0
        );
    }

    // ── AI Insights (Groq) ────────────────────────────────────────────────────

    public List<Map<String, Object>> getAiInsights() {
        String dataSummary = buildDataSummaryForInsights();
        String prompt = """
                You are an expert HR analytics AI. Based on the following real company HR data,
                generate exactly 5 specific, actionable insights for the HR team.

                COMPANY DATA:
                %s

                Return ONLY a valid JSON array with exactly 5 objects:
                [
                  {
                    "type": "<WARNING or INFO or ACTION or SUCCESS>",
                    "title": "<short title under 8 words>",
                    "insight": "<specific observation based on the data, 1-2 sentences>",
                    "action": "<specific recommended action, 1 sentence>",
                    "impact": "<HIGH or MEDIUM or LOW>"
                  }
                ]

                Rules:
                - Be specific — reference actual numbers from the data
                - Mix types: at least 1 WARNING, 1 ACTION, 1 SUCCESS
                - Focus on what matters most for a growing company
                - Return ONLY the JSON array, no markdown, no other text
                """.formatted(dataSummary);

        try {
            String rawResponse = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            String json = rawResponse.trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> insights = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, List.class);
            return insights;
        } catch (Exception e) {
            log.error("AI insights generation failed: {}", e.getMessage());
            return List.of(Map.of(
                    "type", "INFO",
                    "title", "AI insights unavailable",
                    "insight", "Could not generate AI insights at this time.",
                    "action", "Please check the AI service configuration.",
                    "impact", "LOW"
            ));
        }
    }

    private String buildDataSummaryForInsights() {
        int month = LocalDate.now().getMonthValue();
        int year  = LocalDate.now().getYear();

        long total   = employeeRepository.count();
        long active  = employeeRepository.countByStatus(Employee.EmploymentStatus.ACTIVE);
        long openJobs = jobPostingRepository.countByStatus(
                com.PreetishRamola.hrms.recruitment.JobPosting.JobStatus.OPEN);
        long pending  = leaveRepository.countPending();
        long presentThisMonth = attendanceRepository.countTotalPresentDays(month, year);

        List<Object[]> pipeline = candidateRepository.countByPipelineStage();
        Map<String, Long> stages = new HashMap<>();
        for (Object[] row : pipeline) stages.put(row[0].toString(), ((Number) row[1]).longValue());

        Double payrollCost = payrollRepository.sumNetPayByMonthAndYear(month, year);
        Double avgScore     = candidateRepository.avgAiScore();

        long reviewCount = performanceRepository.count();
        OptionalDouble avgRating = performanceRepository.findAll().stream()
                .mapToDouble(p -> p.getRating() != null ? p.getRating().doubleValue() : 0)
                .average();

        double attendanceRate = active > 0 && LocalDate.now().getDayOfMonth() > 0
                ? Math.round((presentThisMonth * 100.0 / (active * LocalDate.now().getDayOfMonth())) * 10.0) / 10.0
                : 0;

        return """
                Employees: %d total, %d active, %d inactive
                Departments: %d
                Open job positions: %d
                Pending leave requests: %d
                Attendance rate this month: %.1f%%
                Monthly payroll cost: ₹%.0f
                Candidates in pipeline: Applied=%d, Screened=%d, Interview=%d, Offer=%d, Hired=%d, Rejected=%d
                Average AI resume score: %.1f/100
                Performance reviews: %d total, avg rating %.1f/5
                """.formatted(
                total, active, total - active,
                departmentRepository.count(),
                openJobs, pending,
                attendanceRate,
                payrollCost != null ? payrollCost : 0.0,
                stages.getOrDefault("APPLIED", 0L),
                stages.getOrDefault("SCREENED", 0L),
                stages.getOrDefault("INTERVIEW", 0L),
                stages.getOrDefault("OFFER", 0L),
                stages.getOrDefault("HIRED", 0L),
                stages.getOrDefault("REJECTED", 0L),
                avgScore != null ? avgScore : 0.0,
                reviewCount,
                avgRating.isPresent() ? avgRating.getAsDouble() : 0.0
        );
    }

    // ── Attrition Risk ────────────────────────────────────────────────────────

    public List<Map<String, Object>> getAttritionRisk() {
        int year  = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        return employeeRepository.findAll().stream()
                .filter(e -> e.getStatus() == Employee.EmploymentStatus.ACTIVE)
                .map(e -> computeRisk(e, year, month))
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("riskScore"), (Double) a.get("riskScore")))
                .collect(Collectors.toList());
    }

    private Map<String, Object> computeRisk(Employee e, int year, int month) {
        List<String> factors = new ArrayList<>();
        double score = 0;

        // Low attendance this month
        long present = attendanceRepository.countPresentDays(e.getId(), month, year);
        long elapsed = LocalDate.now().getDayOfMonth();
        if (elapsed > 0) {
            double rate = present * 100.0 / elapsed;
            if (rate < 60) { score += 30; factors.add("Low attendance (" + (int) rate + "%)"); }
            else if (rate < 80) { score += 15; factors.add("Below-average attendance (" + (int) rate + "%)"); }
        }

        // Many rejected leave requests this year
        List<LeaveRequest> rejected = leaveRepository.findByEmployeeIdAndStatus(
                e.getId(), LeaveRequest.LeaveStatus.REJECTED);
        long rejectedThisYear = rejected.stream()
                .filter(l -> l.getFromDate().getYear() == year).count();
        if (rejectedThisYear >= 3) { score += 15; factors.add("Multiple rejected leave requests (" + rejectedThisYear + ")"); }

        // Low performance rating
        var reviews = performanceRepository.findByEmployeeId(e.getId());
        OptionalDouble avgRating = reviews.stream()
                .filter(r -> r.getYear() == year)
                .mapToDouble(r -> r.getRating() != null ? r.getRating().doubleValue() : 0)
                .average();
        if (avgRating.isPresent()) {
            if (avgRating.getAsDouble() < 2.5) { score += 35; factors.add("Low performance rating (" + String.format("%.1f", avgRating.getAsDouble()) + "/5)"); }
            else if (avgRating.getAsDouble() < 3.5) { score += 15; factors.add("Below-average performance (" + String.format("%.1f", avgRating.getAsDouble()) + "/5)"); }
        }

        // Long tenure in same role (> 3 years, no promotion indicator)
        long tenureMonths = java.time.temporal.ChronoUnit.MONTHS.between(e.getJoinDate(), LocalDate.now());
        if (tenureMonths > 36) { score += 10; factors.add("3+ years without recorded promotion"); }

        // Low salary relative to company
        if (e.getSalary() != null) {
            OptionalDouble avgSalary = employeeRepository.findAll().stream()
                    .filter(emp -> emp.getDepartment() != null
                            && e.getDepartment() != null
                            && emp.getDepartment().getId().equals(e.getDepartment().getId()))
                    .mapToDouble(emp -> emp.getSalary() != null ? emp.getSalary().doubleValue() : 0)
                    .average();
            if (avgSalary.isPresent() && avgSalary.getAsDouble() > 0) {
                double salaryRatio = e.getSalary().doubleValue() / avgSalary.getAsDouble();
                if (salaryRatio < 0.75) { score += 20; factors.add("Salary below department average"); }
            }
        }

        score = Math.min(score, 95);

        String riskLevel = score >= 60 ? "HIGH" : score >= 35 ? "MEDIUM" : "LOW";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("employeeId", e.getId());
        result.put("name", e.getFullName());
        result.put("designation", e.getDesignation());
        result.put("department", e.getDepartment() != null ? e.getDepartment().getName() : "—");
        result.put("riskScore", Math.round(score * 10.0) / 10.0);
        result.put("riskLevel", riskLevel);
        result.put("factors", factors);
        return result;
    }
}
