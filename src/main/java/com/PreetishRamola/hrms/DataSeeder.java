package com.PreetishRamola.hrms;

import com.PreetishRamola.hrms.attendance.Attendance;
import com.PreetishRamola.hrms.attendance.AttendanceRepository;
import com.PreetishRamola.hrms.department.Department;
import com.PreetishRamola.hrms.department.DepartmentRepository;
import com.PreetishRamola.hrms.employee.*;
import com.PreetishRamola.hrms.leave.LeaveRequest;
import com.PreetishRamola.hrms.leave.LeaveRepository;
import com.PreetishRamola.hrms.payroll.Payroll;
import com.PreetishRamola.hrms.payroll.PayrollRepository;
import com.PreetishRamola.hrms.performance.Performance;
import com.PreetishRamola.hrms.performance.PerformanceRepository;
import com.PreetishRamola.hrms.complaint.Complaint;
import com.PreetishRamola.hrms.complaint.ComplaintRepository;
import com.PreetishRamola.hrms.feedback.Feedback;
import com.PreetishRamola.hrms.feedback.FeedbackRepository;
import com.PreetishRamola.hrms.recruitment.Candidate;
import com.PreetishRamola.hrms.recruitment.CandidateRepository;
import com.PreetishRamola.hrms.recruitment.JobPosting;
import com.PreetishRamola.hrms.recruitment.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final PayrollRepository payrollRepository;
    private final PerformanceRepository performanceRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final FeedbackRepository feedbackRepository;
    private final ComplaintRepository complaintRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }

        log.info("🌱 Seeding demo data...");

        // ===== DEPARTMENTS =====
        Department engineering = departmentRepository.save(
                Department.builder().name("Engineering").description("Software development and infrastructure").build());
        Department hr = departmentRepository.save(
                Department.builder().name("Human Resources").description("People, culture and recruitment").build());
        Department sales = departmentRepository.save(
                Department.builder().name("Sales").description("Revenue generation and client relations").build());
        Department finance = departmentRepository.save(
                Department.builder().name("Finance").description("Financial operations and compliance").build());
        Department product = departmentRepository.save(
                Department.builder().name("Product").description("Product strategy and roadmap").build());
        Department marketing = departmentRepository.save(
                Department.builder().name("Marketing").description("Brand, growth and demand generation").build());

        // ===== ADMIN =====
        Employee adminEmp = employeeRepository.save(Employee.builder()
                .firstName("Preetish").lastName("Ramola")
                .email("admin@hrms.com").phone("9876543210")
                .designation("CEO").department(engineering)
                .joinDate(LocalDate.of(2019, 6, 1))
                .salary(BigDecimal.valueOf(200000)).build());
        userRepository.save(User.builder()
                .email("admin@hrms.com")
                .passwordHash(passwordEncoder.encode("admin@123"))
                .role(Role.ROLE_ADMIN).employee(adminEmp).build());

        // ===== MANAGERS =====
        Employee manager1 = employeeRepository.save(Employee.builder()
                .firstName("Arjun").lastName("Sharma")
                .email("manager@hrms.com").phone("9876543211")
                .designation("Engineering Manager").department(engineering)
                .manager(adminEmp).joinDate(LocalDate.of(2020, 3, 15))
                .salary(BigDecimal.valueOf(140000)).build());
        userRepository.save(User.builder()
                .email("manager@hrms.com")
                .passwordHash(passwordEncoder.encode("manager@123"))
                .role(Role.ROLE_MANAGER).employee(manager1).build());

        Employee manager2 = employeeRepository.save(Employee.builder()
                .firstName("Sneha").lastName("Patel")
                .email("sneha.manager@hrms.com").phone("9876543212")
                .designation("HR Manager").department(hr)
                .manager(adminEmp).joinDate(LocalDate.of(2020, 8, 1))
                .salary(BigDecimal.valueOf(110000)).build());
        userRepository.save(User.builder()
                .email("sneha.manager@hrms.com")
                .passwordHash(passwordEncoder.encode("manager@123"))
                .role(Role.ROLE_MANAGER).employee(manager2).build());

        Employee manager3 = employeeRepository.save(Employee.builder()
                .firstName("Karan").lastName("Malhotra")
                .email("karan.manager@hrms.com").phone("9876543220")
                .designation("Product Manager").department(product)
                .manager(adminEmp).joinDate(LocalDate.of(2021, 1, 10))
                .salary(BigDecimal.valueOf(130000)).build());
        userRepository.save(User.builder()
                .email("karan.manager@hrms.com")
                .passwordHash(passwordEncoder.encode("manager@123"))
                .role(Role.ROLE_MANAGER).employee(manager3).build());

        // ===== HR RECRUITER =====
        Employee hrEmp = employeeRepository.save(Employee.builder()
                .firstName("Kavya").lastName("Nair")
                .email("hr@hrms.com").phone("9876543213")
                .designation("HR Recruiter").department(hr)
                .manager(manager2).joinDate(LocalDate.of(2022, 1, 10))
                .salary(BigDecimal.valueOf(70000)).build());
        userRepository.save(User.builder()
                .email("hr@hrms.com")
                .passwordHash(passwordEncoder.encode("hr@123"))
                .role(Role.ROLE_HR).employee(hrEmp).build());

        // ===== EMPLOYEES =====
        Employee emp1 = employeeRepository.save(Employee.builder()
                .firstName("Rohit").lastName("Kumar")
                .email("employee@hrms.com").phone("9876543214")
                .designation("Software Engineer").department(engineering)
                .manager(manager1).joinDate(LocalDate.of(2022, 6, 1))
                .salary(BigDecimal.valueOf(80000)).build());
        userRepository.save(User.builder()
                .email("employee@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_EMPLOYEE).employee(emp1).build());

        Employee emp2 = employeeRepository.save(Employee.builder()
                .firstName("Priya").lastName("Singh")
                .email("priya@hrms.com").phone("9876543215")
                .designation("Senior Developer").department(engineering)
                .manager(manager1).joinDate(LocalDate.of(2021, 9, 15))
                .salary(BigDecimal.valueOf(95000)).build());
        userRepository.save(User.builder().email("priya@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_EMPLOYEE).employee(emp2).build());

        Employee emp3 = employeeRepository.save(Employee.builder()
                .firstName("Vikram").lastName("Mehta")
                .email("vikram@hrms.com").phone("9876543216")
                .designation("Sales Executive").department(sales)
                .manager(adminEmp).joinDate(LocalDate.of(2023, 2, 1))
                .salary(BigDecimal.valueOf(60000)).build());
        userRepository.save(User.builder().email("vikram@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_EMPLOYEE).employee(emp3).build());

        Employee emp4 = employeeRepository.save(Employee.builder()
                .firstName("Ananya").lastName("Gupta")
                .email("ananya@hrms.com").phone("9876543217")
                .designation("Financial Analyst").department(finance)
                .manager(adminEmp).joinDate(LocalDate.of(2022, 11, 20))
                .salary(BigDecimal.valueOf(75000)).build());
        userRepository.save(User.builder().email("ananya@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_EMPLOYEE).employee(emp4).build());

        Employee emp5 = employeeRepository.save(Employee.builder()
                .firstName("Nikhil").lastName("Bose")
                .email("nikhil@hrms.com").phone("9876543218")
                .designation("DevOps Engineer").department(engineering)
                .manager(manager1).joinDate(LocalDate.of(2021, 5, 3))
                .salary(BigDecimal.valueOf(105000)).build());
        userRepository.save(User.builder().email("nikhil@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_EMPLOYEE).employee(emp5).build());

        Employee emp6 = employeeRepository.save(Employee.builder()
                .firstName("Pooja").lastName("Iyer")
                .email("pooja@hrms.com").phone("9876543219")
                .designation("Product Designer").department(product)
                .manager(manager3).joinDate(LocalDate.of(2023, 3, 20))
                .salary(BigDecimal.valueOf(78000)).build());
        userRepository.save(User.builder().email("pooja@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_EMPLOYEE).employee(emp6).build());

        Employee emp7 = employeeRepository.save(Employee.builder()
                .firstName("Ravi").lastName("Teja")
                .email("ravi@hrms.com").phone("9876543221")
                .designation("Sales Manager").department(sales)
                .manager(adminEmp).joinDate(LocalDate.of(2020, 7, 14))
                .salary(BigDecimal.valueOf(115000)).build());
        userRepository.save(User.builder().email("ravi@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_MANAGER).employee(emp7).build());

        Employee emp8 = employeeRepository.save(Employee.builder()
                .firstName("Divya").lastName("Reddy")
                .email("divya@hrms.com").phone("9876543222")
                .designation("Marketing Lead").department(marketing)
                .manager(adminEmp).joinDate(LocalDate.of(2022, 4, 5))
                .salary(BigDecimal.valueOf(88000)).build());
        userRepository.save(User.builder().email("divya@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_MANAGER).employee(emp8).build());

        Employee emp9 = employeeRepository.save(Employee.builder()
                .firstName("Amit").lastName("Shah")
                .email("amit@hrms.com").phone("9876543223")
                .designation("Backend Engineer").department(engineering)
                .manager(manager1).joinDate(LocalDate.of(2023, 8, 1))
                .salary(BigDecimal.valueOf(72000)).build());
        userRepository.save(User.builder().email("amit@hrms.com")
                .passwordHash(passwordEncoder.encode("employee@123"))
                .role(Role.ROLE_EMPLOYEE).employee(emp9).build());

        // ===== DEPARTMENT HEADS =====
        engineering.setHeadId(manager1.getId());
        hr.setHeadId(manager2.getId());
        product.setHeadId(manager3.getId());
        sales.setHeadId(emp7.getId());
        marketing.setHeadId(emp8.getId());
        departmentRepository.save(engineering);
        departmentRepository.save(hr);
        departmentRepository.save(product);
        departmentRepository.save(sales);
        departmentRepository.save(marketing);

        // ===== ATTENDANCE (last 30 working days) =====
        List<Employee> allEmps = List.of(emp1, emp2, emp3, emp4, emp5, emp6, emp7, emp8, emp9, manager1, manager2, manager3, hrEmp);

        for (int i = 30; i >= 1; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            // Skip weekends
            if (date.getDayOfWeek().getValue() >= 6) continue;

            for (Employee e : allEmps) {
                if (attendanceRepository.existsByEmployeeIdAndDate(e.getId(), date)) continue;

                // Introduce some variety: ~5% absent, ~5% half-day, few late arrivals
                int hash = (int) ((e.getId() * 31 + i * 7) % 100);
                Attendance.AttendanceStatus status;
                LocalDateTime checkIn;
                LocalDateTime checkOut;
                BigDecimal hours;

                if (hash < 5) {
                    // Absent
                    status = Attendance.AttendanceStatus.ABSENT;
                    checkIn = null;
                    checkOut = null;
                    hours = BigDecimal.ZERO;
                } else if (hash < 10) {
                    // Half day
                    status = Attendance.AttendanceStatus.HALF_DAY;
                    checkIn = date.atTime(9, 0);
                    checkOut = date.atTime(13, 30);
                    hours = BigDecimal.valueOf(4.5);
                } else if (hash < 18) {
                    // Late arrival
                    status = Attendance.AttendanceStatus.PRESENT;
                    checkIn = date.atTime(10, 30);
                    checkOut = date.atTime(19, 30);
                    hours = BigDecimal.valueOf(9.0);
                } else {
                    // Normal
                    status = Attendance.AttendanceStatus.PRESENT;
                    checkIn = date.atTime(9, 0);
                    checkOut = date.atTime(18, 0);
                    hours = BigDecimal.valueOf(9.0);
                }

                attendanceRepository.save(Attendance.builder()
                        .employee(e).date(date)
                        .checkIn(checkIn).checkOut(checkOut)
                        .hoursWorked(hours).status(status).build());
            }
        }

        // ===== LEAVE REQUESTS =====
        // Pending
        leaveRepository.save(LeaveRequest.builder()
                .employee(emp1).leaveType(LeaveRequest.LeaveType.SICK)
                .fromDate(LocalDate.now().plusDays(3)).toDate(LocalDate.now().plusDays(4))
                .reason("Fever and cold").status(LeaveRequest.LeaveStatus.PENDING).build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(emp3).leaveType(LeaveRequest.LeaveType.CASUAL)
                .fromDate(LocalDate.now().plusDays(7)).toDate(LocalDate.now().plusDays(7))
                .reason("Family function").status(LeaveRequest.LeaveStatus.PENDING).build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(emp6).leaveType(LeaveRequest.LeaveType.EARNED)
                .fromDate(LocalDate.now().plusDays(14)).toDate(LocalDate.now().plusDays(18))
                .reason("Annual vacation — Europe trip").status(LeaveRequest.LeaveStatus.PENDING).build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(emp9).leaveType(LeaveRequest.LeaveType.CASUAL)
                .fromDate(LocalDate.now().plusDays(2)).toDate(LocalDate.now().plusDays(2))
                .reason("Personal work").status(LeaveRequest.LeaveStatus.PENDING).build());

        // Approved
        leaveRepository.save(LeaveRequest.builder()
                .employee(emp2).leaveType(LeaveRequest.LeaveType.CASUAL)
                .fromDate(LocalDate.now().minusDays(10)).toDate(LocalDate.now().minusDays(10))
                .reason("Personal work").status(LeaveRequest.LeaveStatus.APPROVED)
                .approvedBy(manager1).actionedAt(LocalDateTime.now().minusDays(11)).build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(emp5).leaveType(LeaveRequest.LeaveType.EARNED)
                .fromDate(LocalDate.now().minusDays(20)).toDate(LocalDate.now().minusDays(16))
                .reason("Wedding anniversary trip").status(LeaveRequest.LeaveStatus.APPROVED)
                .approvedBy(manager1).actionedAt(LocalDateTime.now().minusDays(25)).build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(hrEmp).leaveType(LeaveRequest.LeaveType.SICK)
                .fromDate(LocalDate.now().minusDays(5)).toDate(LocalDate.now().minusDays(4))
                .reason("Viral infection").status(LeaveRequest.LeaveStatus.APPROVED)
                .approvedBy(manager2).actionedAt(LocalDateTime.now().minusDays(6)).build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(emp4).leaveType(LeaveRequest.LeaveType.MATERNITY)
                .fromDate(LocalDate.now().plusDays(30)).toDate(LocalDate.now().plusDays(120))
                .reason("Maternity leave").status(LeaveRequest.LeaveStatus.APPROVED)
                .approvedBy(adminEmp).actionedAt(LocalDateTime.now().minusDays(2)).build());

        // Rejected
        leaveRepository.save(LeaveRequest.builder()
                .employee(emp3).leaveType(LeaveRequest.LeaveType.EARNED)
                .fromDate(LocalDate.now().plusDays(1)).toDate(LocalDate.now().plusDays(5))
                .reason("Extended vacation").status(LeaveRequest.LeaveStatus.REJECTED)
                .rejectionReason("Critical project deadline this week — please reschedule")
                .approvedBy(adminEmp).actionedAt(LocalDateTime.now().minusDays(1)).build());

        leaveRepository.save(LeaveRequest.builder()
                .employee(emp9).leaveType(LeaveRequest.LeaveType.UNPAID)
                .fromDate(LocalDate.now().minusDays(3)).toDate(LocalDate.now().minusDays(1))
                .reason("Personal emergency").status(LeaveRequest.LeaveStatus.REJECTED)
                .rejectionReason("Insufficient leave balance — discussed alternative arrangement")
                .approvedBy(manager1).actionedAt(LocalDateTime.now().minusDays(4)).build());

        // ===== PAYROLL (last 2 months + current month) =====
        int thisMonth = LocalDate.now().getMonthValue();
        int lastMonth = thisMonth == 1 ? 12 : thisMonth - 1;
        int twoMonthsAgo = lastMonth == 1 ? 12 : lastMonth - 1;
        int thisYear = LocalDate.now().getYear();
        int lastYear = thisMonth == 1 ? thisYear - 1 : thisYear;

        List<Employee> payrollEmps = List.of(emp1, emp2, emp3, emp4, emp5, emp6, emp7, emp8, emp9, manager1, manager2, manager3, hrEmp);
        for (Employee e : payrollEmps) {
            BigDecimal basic = e.getSalary();
            BigDecimal hra = basic.multiply(BigDecimal.valueOf(0.40)).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal allowances = basic.multiply(BigDecimal.valueOf(0.20)).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal deductions = basic.multiply(BigDecimal.valueOf(0.22)).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal netPay = basic.add(hra).add(allowances).subtract(deductions).setScale(2, java.math.RoundingMode.HALF_UP);

            // Two months ago
            payrollRepository.save(Payroll.builder()
                    .employee(e).month(twoMonthsAgo).year(twoMonthsAgo == 12 ? lastYear - 1 : thisYear)
                    .basicSalary(basic).hra(hra).allowances(allowances)
                    .deductions(deductions).netPay(netPay)
                    .workingDays(BigDecimal.valueOf(26)).presentDays(BigDecimal.valueOf(25)).build());

            // Last month
            payrollRepository.save(Payroll.builder()
                    .employee(e).month(lastMonth).year(lastYear)
                    .basicSalary(basic).hra(hra).allowances(allowances)
                    .deductions(deductions).netPay(netPay)
                    .workingDays(BigDecimal.valueOf(26)).presentDays(BigDecimal.valueOf(26)).build());

            // Current month
            payrollRepository.save(Payroll.builder()
                    .employee(e).month(thisMonth).year(thisYear)
                    .basicSalary(basic).hra(hra).allowances(allowances)
                    .deductions(deductions).netPay(netPay)
                    .workingDays(BigDecimal.valueOf(26)).presentDays(BigDecimal.valueOf(20)).build());
        }

        // ===== PERFORMANCE REVIEWS =====
        int prevYear = thisYear - 1;

        // Q4 last year — acknowledged
        performanceRepository.save(Performance.builder()
                .employee(emp1).reviewer(manager1).quarter(4).year(prevYear)
                .rating(BigDecimal.valueOf(3.9))
                .goals("Complete microservices migration. Write unit tests for all APIs.")
                .achievements("Migrated 4 services to microservices. Test coverage at 65%.")
                .comments("Good progress, but test coverage goal was partially met.")
                .status(Performance.ReviewStatus.ACKNOWLEDGED)
                .submittedAt(LocalDateTime.now().minusDays(90)).build());

        performanceRepository.save(Performance.builder()
                .employee(emp2).reviewer(manager1).quarter(4).year(prevYear)
                .rating(BigDecimal.valueOf(4.5))
                .goals("Lead the frontend redesign. Establish component library.")
                .achievements("Redesign shipped on time. Storybook component library created with 40+ components.")
                .comments("Outstanding delivery. Priya is a key contributor to team quality.")
                .status(Performance.ReviewStatus.ACKNOWLEDGED)
                .submittedAt(LocalDateTime.now().minusDays(90)).build());

        // Q1 this year — submitted
        performanceRepository.save(Performance.builder()
                .employee(emp1).reviewer(manager1).quarter(1).year(thisYear)
                .rating(BigDecimal.valueOf(4.2))
                .goals("Complete microservices migration. Improve test coverage to 80%.")
                .achievements("Delivered 3 major features. Reduced API latency by 30%. Coverage now at 78%.")
                .comments("Rohit is a strong performer with great technical skills.")
                .status(Performance.ReviewStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusDays(30)).build());

        performanceRepository.save(Performance.builder()
                .employee(emp2).reviewer(manager1).quarter(1).year(thisYear)
                .rating(BigDecimal.valueOf(4.7))
                .goals("Lead frontend redesign. Mentor junior developers.")
                .achievements("Successfully led UI overhaul. Mentored 2 junior devs. Zero prod bugs in Q1.")
                .comments("Exceptional performance this quarter.")
                .status(Performance.ReviewStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusDays(30)).build());

        performanceRepository.save(Performance.builder()
                .employee(emp5).reviewer(manager1).quarter(1).year(thisYear)
                .rating(BigDecimal.valueOf(4.4))
                .goals("Implement CI/CD pipeline. Reduce deployment time by 50%.")
                .achievements("Full CI/CD on GitHub Actions. Deployment time reduced from 45 min to 12 min.")
                .comments("Nikhil has transformed our deployment process. Excellent work.")
                .status(Performance.ReviewStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusDays(28)).build());

        performanceRepository.save(Performance.builder()
                .employee(emp3).reviewer(emp7).quarter(1).year(thisYear)
                .rating(BigDecimal.valueOf(3.5))
                .goals("Achieve ₹15L monthly revenue target. Expand client base by 20%.")
                .achievements("₹12.4L revenue (83% of target). 4 new clients acquired.")
                .comments("Short of target but positive trajectory. Focus needed on enterprise accounts.")
                .status(Performance.ReviewStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusDays(25)).build());

        performanceRepository.save(Performance.builder()
                .employee(emp4).reviewer(adminEmp).quarter(1).year(thisYear)
                .rating(BigDecimal.valueOf(4.1))
                .goals("Implement budget tracking dashboard. Complete statutory compliance audit.")
                .achievements("Dashboard live. All compliance docs filed 2 weeks ahead of deadline.")
                .comments("Ananya keeps the finance function running with precision.")
                .status(Performance.ReviewStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now().minusDays(20)).build());

        // Q2 this year — draft (in progress)
        performanceRepository.save(Performance.builder()
                .employee(emp1).reviewer(manager1).quarter(2).year(thisYear)
                .rating(null)
                .goals("Ship new payments module. Onboard 2 engineers.")
                .achievements(null).comments(null)
                .status(Performance.ReviewStatus.DRAFT).build());

        performanceRepository.save(Performance.builder()
                .employee(emp6).reviewer(manager3).quarter(2).year(thisYear)
                .rating(null)
                .goals("Design new mobile app screens. Run 3 usability testing rounds.")
                .achievements(null).comments(null)
                .status(Performance.ReviewStatus.DRAFT).build());

        performanceRepository.save(Performance.builder()
                .employee(emp9).reviewer(manager1).quarter(2).year(thisYear)
                .rating(null)
                .goals("Refactor legacy auth service. Achieve 90% test coverage.")
                .achievements(null).comments(null)
                .status(Performance.ReviewStatus.DRAFT).build());

        // ===== JOB POSTINGS =====
        JobPosting job1 = jobPostingRepository.save(JobPosting.builder()
                .title("Senior Backend Developer")
                .department(engineering)
                .description("We are looking for a senior backend developer to design and implement scalable microservices. " +
                        "You will own core API services, mentor junior engineers, participate in architecture decisions, " +
                        "and ensure production reliability. Must have experience with high-traffic distributed systems.")
                .requiredSkills(List.of("Java", "Spring Boot", "PostgreSQL", "Redis", "Docker"))
                .experienceYears(4).salaryRange("80,000 - 120,000").status(JobPosting.JobStatus.OPEN).build());

        JobPosting job2 = jobPostingRepository.save(JobPosting.builder()
                .title("Full Stack Developer")
                .department(engineering)
                .description("Join our product team as a full stack developer. You will build React frontends backed by " +
                        "Node.js APIs, collaborate closely with designers and product managers, and take features from " +
                        "design to production. We value clean code, great UX instincts, and rapid iteration.")
                .requiredSkills(List.of("React", "Node.js", "TypeScript", "MongoDB"))
                .experienceYears(3).salaryRange("70,000 - 100,000").status(JobPosting.JobStatus.OPEN).build());

        JobPosting job3 = jobPostingRepository.save(JobPosting.builder()
                .title("DevOps Engineer")
                .department(engineering)
                .description("We need a DevOps engineer to own our cloud infrastructure, CI/CD pipelines, and observability " +
                        "stack. You'll work with Kubernetes, Terraform, and AWS to ensure 99.9% uptime across all services. " +
                        "Experience with incident response and on-call rotations is essential.")
                .requiredSkills(List.of("Kubernetes", "Docker", "Terraform", "AWS", "CI/CD", "Linux"))
                .experienceYears(3).salaryRange("85,000 - 115,000").status(JobPosting.JobStatus.OPEN).build());

        JobPosting job4 = jobPostingRepository.save(JobPosting.builder()
                .title("Product Manager")
                .department(product)
                .description("Drive the product roadmap for our core platform. You will define requirements by working with " +
                        "customers and engineers, write detailed PRDs, prioritise the backlog, and measure success with data. " +
                        "Must have experience shipping B2B SaaS products and running agile sprints.")
                .requiredSkills(List.of("Product Strategy", "Agile", "SQL", "User Research", "Roadmap Planning"))
                .experienceYears(4).salaryRange("90,000 - 130,000").status(JobPosting.JobStatus.OPEN).build());

        JobPosting job5 = jobPostingRepository.save(JobPosting.builder()
                .title("Marketing Growth Manager")
                .department(marketing)
                .description("Own acquisition, activation and retention across all channels. You'll run paid and organic " +
                        "campaigns, manage SEO strategy, work with the content team, and report weekly growth metrics to " +
                        "leadership. Startup experience and strong analytics skills are a must.")
                .requiredSkills(List.of("SEO", "Google Ads", "Content Marketing", "Analytics", "HubSpot"))
                .experienceYears(3).salaryRange("65,000 - 95,000").status(JobPosting.JobStatus.OPEN).build());

        // Closed posting — to test closed-job display
        JobPosting job6 = jobPostingRepository.save(JobPosting.builder()
                .title("Junior Frontend Developer")
                .department(engineering)
                .description("Entry-level frontend role for a React developer excited about building great UIs.")
                .requiredSkills(List.of("React", "JavaScript", "CSS", "HTML"))
                .experienceYears(1).salaryRange("45,000 - 60,000").status(JobPosting.JobStatus.CLOSED).build());

        // ===== CANDIDATES =====

        // --- Job 1: Senior Backend Developer ---
        candidateRepository.save(Candidate.builder()
                .jobPosting(job1).name("Aditya Verma").email("aditya.v@gmail.com").phone("9876501234")
                .aiScore(BigDecimal.valueOf(88.5)).skillMatchPercent(BigDecimal.valueOf(90.0))
                .aiSummary("5 years of Java and Spring Boot in fintech. Strong Redis and Docker usage in production. " +
                        "Excellent system design fundamentals with distributed systems experience.")
                .aiStrengths("[\"5 years production Java\",\"Redis expertise\",\"Microservices architecture\",\"Strong system design\"]")
                .aiGaps("[\"No Kubernetes experience\"]")
                .aiRecommendation("STRONG_YES")
                .pipelineStage(Candidate.PipelineStage.SCREENED).build());

        candidateRepository.save(Candidate.builder()
                .jobPosting(job1).name("Meera Joshi").email("meera.j@gmail.com").phone("9876501235")
                .aiScore(BigDecimal.valueOf(75.0)).skillMatchPercent(BigDecimal.valueOf(78.0))
                .aiSummary("Solid Java developer with 4 years experience. Good Spring Boot knowledge, moderate PostgreSQL. " +
                        "Redis exposure is limited to caching basics but fundamentals are strong.")
                .aiStrengths("[\"4 years Java\",\"Spring Security expertise\",\"Good PostgreSQL knowledge\"]")
                .aiGaps("[\"Limited Redis usage\",\"No Docker in production\"]")
                .aiRecommendation("YES")
                .pipelineStage(Candidate.PipelineStage.INTERVIEW)
                .scheduledInterviewAt(LocalDateTime.now().plusDays(2))
                .interviewMeetingLink("https://meet.google.com/abc-defg-hij").build());

        candidateRepository.save(Candidate.builder()
                .jobPosting(job1).name("Saurabh Tiwari").email("saurabh.t@gmail.com").phone("9876501240")
                .aiScore(BigDecimal.valueOf(82.0)).skillMatchPercent(BigDecimal.valueOf(85.0))
                .aiSummary("Backend engineer with 5 years at a high-growth startup. Deep PostgreSQL tuning experience, " +
                        "Docker-first development workflow. Would be a strong addition to the team.")
                .aiStrengths("[\"5 years backend\",\"PostgreSQL query optimisation\",\"Docker expert\",\"Startup pedigree\"]")
                .aiGaps("[\"Spring Boot — used earlier versions, may need upskilling on v3\"]")
                .aiRecommendation("YES")
                .pipelineStage(Candidate.PipelineStage.OFFER)
                .scheduledInterviewAt(LocalDateTime.now().minusDays(5))
                .interviewMeetingLink("https://meet.google.com/xyz-uvwx-yz").build());

        candidateRepository.save(Candidate.builder()
                .jobPosting(job1).name("Divya Kapoor").email("divya.k@gmail.com").phone("9876501237")
                .aiScore(BigDecimal.valueOf(38.0)).skillMatchPercent(BigDecimal.valueOf(32.0))
                .aiSummary("3 years of PHP development. Limited Java exposure through coursework only. Missing most " +
                        "required technical skills for this senior role.")
                .aiStrengths("[\"Strong problem solving\",\"REST API knowledge\"]")
                .aiGaps("[\"No Java production experience\",\"No Spring Boot\",\"No Redis or Docker\"]")
                .aiRecommendation("NO")
                .pipelineStage(Candidate.PipelineStage.REJECTED).build());

        candidateRepository.save(Candidate.builder()
                .jobPosting(job1).name("Harish Nair").email("harish.n@gmail.com").phone("9876501241")
                .pipelineStage(Candidate.PipelineStage.APPLIED).build());

        // --- Job 2: Full Stack Developer ---
        candidateRepository.save(Candidate.builder()
                .jobPosting(job2).name("Rahul Sharma").email("rahul.s@gmail.com").phone("9876501236")
                .aiScore(BigDecimal.valueOf(92.0)).skillMatchPercent(BigDecimal.valueOf(95.0))
                .aiSummary("Exceptional React and Node.js skills with 4 years of full-stack development at a product company. " +
                        "TypeScript throughout. MongoDB advanced usage including aggregation pipelines.")
                .aiStrengths("[\"4 years full-stack\",\"TypeScript expert\",\"React performance optimisation\",\"MongoDB aggregation\"]")
                .aiGaps("[]")
                .aiRecommendation("STRONG_YES")
                .pipelineStage(Candidate.PipelineStage.HIRED)
                .scheduledInterviewAt(LocalDateTime.now().minusDays(10)).build());

        candidateRepository.save(Candidate.builder()
                .jobPosting(job2).name("Simran Kaur").email("simran.k@gmail.com").phone("9876501242")
                .aiScore(BigDecimal.valueOf(70.0)).skillMatchPercent(BigDecimal.valueOf(72.0))
                .aiSummary("3 years React experience with decent Node.js. TypeScript is a recent addition to her stack. " +
                        "MongoDB experience is mostly read-heavy CRUD — no complex pipelines.")
                .aiStrengths("[\"Strong React component design\",\"Good testing habits (Jest/RTL)\"]")
                .aiGaps("[\"TypeScript experience < 1 year\",\"Limited backend Node.js depth\"]")
                .aiRecommendation("YES")
                .pipelineStage(Candidate.PipelineStage.SCREENED).build());

        candidateRepository.save(Candidate.builder()
                .jobPosting(job2).name("Yash Patel").email("yash.p@gmail.com").phone("9876501243")
                .aiScore(BigDecimal.valueOf(55.0)).skillMatchPercent(BigDecimal.valueOf(58.0))
                .aiSummary("2 years of React development. Limited Node.js experience, mostly tutorials. " +
                        "TypeScript adoption is very recent. May be better suited for a junior role.")
                .aiStrengths("[\"React fundamentals solid\",\"Good UI/UX sensibility\"]")
                .aiGaps("[\"2 years total — below 3 year requirement\",\"Node.js not production-ready\",\"No MongoDB\"]")
                .aiRecommendation("MAYBE")
                .pipelineStage(Candidate.PipelineStage.REJECTED).build());

        // --- Job 3: DevOps Engineer ---
        candidateRepository.save(Candidate.builder()
                .jobPosting(job3).name("Kiran Desai").email("kiran.d@gmail.com").phone("9876501244")
                .aiScore(BigDecimal.valueOf(89.0)).skillMatchPercent(BigDecimal.valueOf(91.0))
                .aiSummary("DevOps engineer with 4 years managing Kubernetes clusters on AWS. Terraform expert, " +
                        "built CI/CD for a team of 80 engineers. Incident response experience at scale.")
                .aiStrengths("[\"Kubernetes CKA certified\",\"Terraform modules expert\",\"AWS Solutions Architect\",\"On-call experience\"]")
                .aiGaps("[\"Limited GitLab CI — uses GitHub Actions and CircleCI\"]")
                .aiRecommendation("STRONG_YES")
                .pipelineStage(Candidate.PipelineStage.INTERVIEW)
                .scheduledInterviewAt(LocalDateTime.now().plusDays(3))
                .interviewMeetingLink("https://zoom.us/j/98765432").build());

        candidateRepository.save(Candidate.builder()
                .jobPosting(job3).name("Aryan Gupta").email("aryan.g@gmail.com").phone("9876501245")
                .pipelineStage(Candidate.PipelineStage.APPLIED).build());

        // --- Job 4: Product Manager ---
        candidateRepository.save(Candidate.builder()
                .jobPosting(job4).name("Nisha Agarwal").email("nisha.a@gmail.com").phone("9876501246")
                .aiScore(BigDecimal.valueOf(80.0)).skillMatchPercent(BigDecimal.valueOf(82.0))
                .aiSummary("Product Manager with 5 years at B2B SaaS companies. Led roadmap for a product with 200K MAU. " +
                        "Strong user research background, comfortable with SQL for data analysis.")
                .aiStrengths("[\"5 years B2B PM\",\"User research expert\",\"SQL proficient\",\"Agile certified (PMI-ACP)\"]")
                .aiGaps("[\"No experience with roadmap tools beyond Jira\"]")
                .aiRecommendation("YES")
                .pipelineStage(Candidate.PipelineStage.SCREENED).build());

        // --- Job 5: Marketing Growth Manager ---
        candidateRepository.save(Candidate.builder()
                .jobPosting(job5).name("Priya Menon").email("priya.m@gmail.com").phone("9876501247")
                .aiScore(BigDecimal.valueOf(77.0)).skillMatchPercent(BigDecimal.valueOf(80.0))
                .aiSummary("Growth marketer with 3 years running SEO and paid campaigns. Grew organic traffic 3x at previous " +
                        "startup. HubSpot certified, strong Google Analytics and Ads expertise.")
                .aiStrengths("[\"SEO — 3x organic growth track record\",\"Google Ads certified\",\"HubSpot expert\",\"Data-driven mindset\"]")
                .aiGaps("[\"Content strategy experience is limited\"]")
                .aiRecommendation("YES")
                .pipelineStage(Candidate.PipelineStage.INTERVIEW)
                .scheduledInterviewAt(LocalDateTime.now().plusDays(1))
                .interviewMeetingLink("https://meet.google.com/mno-pqrs-tuv").build());

        // ===== FEEDBACK =====
        feedbackRepository.save(Feedback.builder()
                .fromEmployee(manager1).toEmployee(emp1)
                .category(Feedback.FeedbackCategory.RECOGNITION)
                .content("Rohit, great work on the API performance improvements this sprint. The latency reduction was noticeable and the team really appreciated it. Keep it up!").build());

        feedbackRepository.save(Feedback.builder()
                .fromEmployee(emp2).toEmployee(emp1)
                .category(Feedback.FeedbackCategory.COLLABORATION)
                .content("Really enjoyed pairing with you on the auth module refactor. Your approach to breaking down the problem was methodical and helped me learn a lot. Would love to collaborate more.").build());

        feedbackRepository.save(Feedback.builder()
                .fromEmployee(adminEmp).toEmployee(manager1)
                .category(Feedback.FeedbackCategory.RECOGNITION)
                .content("Arjun, the engineering team has been delivering consistently since you took over. The CI/CD improvements and the culture of code review you've built are visible in the quality of releases.").build());

        feedbackRepository.save(Feedback.builder()
                .fromEmployee(manager1).toEmployee(emp2)
                .category(Feedback.FeedbackCategory.IMPROVEMENT)
                .content("Priya, you do excellent technical work. One area to grow — try to communicate blockers earlier. There were a couple of times this quarter where a heads-up a day earlier would have helped the team re-plan.").build());

        feedbackRepository.save(Feedback.builder()
                .fromEmployee(emp1).toEmployee(emp5)
                .category(Feedback.FeedbackCategory.RECOGNITION)
                .content("Nikhil, the new deployment pipeline is a game changer. Deployments used to be stressful — now they're boring (in the best way). Thanks for making everyone's life easier.").build());

        feedbackRepository.save(Feedback.builder()
                .fromEmployee(manager2).toEmployee(hrEmp)
                .category(Feedback.FeedbackCategory.RECOGNITION)
                .content("Kavya, the onboarding process you redesigned is so much smoother now. New joiners have been giving great feedback. Thank you for taking the initiative.").build());

        feedbackRepository.save(Feedback.builder()
                .fromEmployee(emp5).toEmployee(emp9)
                .category(Feedback.FeedbackCategory.IMPROVEMENT)
                .content("Amit, your code quality is good but please improve your PR descriptions. Reviewers often have to dig into the code to understand the context. A short summary and motivation in each PR would really help the team.").build());

        // ===== COMPLAINTS =====
        complaintRepository.save(Complaint.builder()
                .category(Complaint.ComplaintCategory.WORK_ENVIRONMENT)
                .description("The office AC in the Engineering wing has been broken for over 3 weeks now. Multiple requests have been submitted to facilities but nothing has been done. The temperature makes it very uncomfortable and affects productivity.")
                .status(Complaint.ComplaintStatus.UNDER_REVIEW)
                .hrNotes("Escalated to facilities team on 2nd June. Follow-up scheduled for 10th June.").build());

        complaintRepository.save(Complaint.builder()
                .category(Complaint.ComplaintCategory.POLICY_VIOLATION)
                .description("A senior team member has been taking credit for work done by junior engineers in sprint reviews without acknowledging their contributions. This has happened at least 4 times in the past 2 months and is demoralising for the team.")
                .status(Complaint.ComplaintStatus.OPEN).build());

        complaintRepository.save(Complaint.builder()
                .category(Complaint.ComplaintCategory.HARASSMENT)
                .description("I have been receiving unwanted messages outside of work hours from someone in my department. The tone is inappropriate and I have already asked them to stop but it has continued. I am not comfortable escalating directly due to their seniority.")
                .status(Complaint.ComplaintStatus.UNDER_REVIEW)
                .hrNotes("Confidential investigation initiated. Interview scheduled with relevant parties.").build());

        complaintRepository.save(Complaint.builder()
                .category(Complaint.ComplaintCategory.MISCONDUCT)
                .description("A manager was overheard making dismissive and belittling comments about an employee's work in front of others during a team lunch. This created a very uncomfortable atmosphere for everyone present.")
                .status(Complaint.ComplaintStatus.RESOLVED)
                .hrNotes("Discussion held with the individual. Formal counselling session completed. Behaviour monitoring ongoing.")
                .resolvedAt(LocalDateTime.now().minusDays(5)).build());

        complaintRepository.save(Complaint.builder()
                .category(Complaint.ComplaintCategory.SAFETY)
                .description("The fire exit on floor 2 has been blocked by stored equipment for the past month. This is a serious safety hazard. I have raised it verbally but nothing has changed.")
                .status(Complaint.ComplaintStatus.RESOLVED)
                .hrNotes("Facilities team cleared the exit on the same day this was reported. Permanent fix implemented.")
                .resolvedAt(LocalDateTime.now().minusDays(12)).build());

        log.info("✅ Demo data seeded successfully!");
        log.info("─────────────────────────────────────────");
        log.info("📧 Login credentials:");
        log.info("   ADMIN    → admin@hrms.com           / admin@123");
        log.info("   MANAGER  → manager@hrms.com         / manager@123");
        log.info("              sneha.manager@hrms.com   / manager@123");
        log.info("              karan.manager@hrms.com   / manager@123");
        log.info("   HR       → hr@hrms.com              / hr@123");
        log.info("   EMPLOYEE → employee@hrms.com        / employee@123");
        log.info("─────────────────────────────────────────");
        log.info("🏢 Departments: Engineering, HR, Sales, Finance, Product, Marketing");
        log.info("👥 Employees: 13 total across all departments");
        log.info("📋 Job Postings: 5 open + 1 closed");
        log.info("🎯 Candidates: 15 across all pipeline stages");
        log.info("📅 Attendance: 30 days with absences, half-days, late arrivals");
        log.info("🌴 Leave Requests: 10 (4 pending, 4 approved, 2 rejected)");
        log.info("💰 Payroll: 3 months for all 13 employees");
        log.info("⭐ Performance Reviews: Q4 last year + Q1 this year + Q2 drafts");
        log.info("💬 Feedback: 7 peer feedback entries across all roles");
        log.info("🔒 Complaints: 5 anonymous complaints (open, reviewing, resolved)");
    }
}
