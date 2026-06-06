# HRMS — Human Resource Management System

A full-stack, AI-powered HRMS built with **Spring Boot 3.3**, **PostgreSQL**, **Redis**, and **Groq AI**. Manages the complete employee lifecycle — from recruitment and onboarding to attendance, payroll, performance reviews, and offboarding.

> **Frontend repo:** [HRMS-Frontend](https://github.com/preetishverb/HRMS-Frontend)

---

## Features

### Core HR Modules
- **Employee Management** — Create, update, deactivate employees with department and manager hierarchy
- **Attendance Tracking** — Daily check-in/out with monthly summaries and status tracking (Present, Absent, Late, Half Day)
- **Leave Management** — Apply, approve, and reject leaves with balance tracking (Casual 12, Sick 7, Earned 15)
- **Payroll Generation** — Monthly payroll with Basic, HRA (40%), Allowances (20%), deductions, and net pay
- **Performance Reviews** — Quarterly reviews with ratings (1–5), goals, achievements, and manager comments

### AI-Powered Features
- **Resume Screening** — Auto-screens uploaded PDFs using Groq (Llama 3.3 70B). Dual gate: skill match % + years of experience threshold
- **Adaptive AI Interviews** — Dynamic question generation based on job role and candidate profile
- **HR Chatbot** — Context-aware assistant with access to real-time employee data (leave balance, attendance, payslips). Redis-backed session memory
- **Offer Letter Generation** — Auto-generates and emails offer letters to shortlisted candidates

### Recruitment Pipeline
- Job postings with required skills, salary range, experience requirements
- Candidate pipeline: `WAITING → SHORTLISTED → INTERVIEW → OFFER → HIRED / REJECTED`
- Resume upload via Cloudinary
- Public careers page (no auth required)
- Candidate offer accept/decline via email token

### People & Culture
- **Peer Feedback** — Any role can give feedback to any employee; visible only to the recipient
- **Anonymous Complaints** — Employees submit complaints without identity stored; HR manages resolution

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.3.5 |
| Language | Java 21 |
| Security | Spring Security + JWT |
| Database | PostgreSQL 16 |
| Cache / Sessions | Redis 7 |
| AI | Spring AI + Groq (Llama 3.3 70B) |
| File Storage | Cloudinary |
| Email | Spring Mail (Gmail SMTP) |
| Build | Maven |

---

## Getting Started

### Prerequisites
- Java 21
- Docker Desktop (for PostgreSQL + Redis)
- Maven

### 1. Clone the repo
```bash
git clone https://github.com/preetishverb/HRMS.git
cd HRMS
```

### 2. Configure environment
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

Fill in your values in `application.yml`:
- PostgreSQL credentials
- Redis host/port
- Groq API key
- Cloudinary credentials
- Gmail SMTP credentials

### 3. Start infrastructure
```bash
docker-compose up -d
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

---

## Demo Credentials

| Role | Email | Password |
|---|---|---|
| Admin | admin@hrms.com | admin@123 |
| Manager | manager@hrms.com | manager@123 |
| HR | hr@hrms.com | hr@123 |
| Employee | employee@hrms.com | employee@123 |

---

## API Overview

```
POST   /api/auth/login                          # Login
GET    /api/employees                           # List employees (Admin/HR/Manager)
POST   /api/employees                           # Create employee (Admin/HR)
GET    /api/employees/{id}/attendance           # Monthly attendance
POST   /api/employees/{id}/leaves              # Apply leave
GET    /api/leaves/pending                      # Pending leaves (HR/Manager)
POST   /api/payroll/generate                    # Generate monthly payroll (Admin)
GET    /api/employees/{id}/performance          # Performance reviews
POST   /api/jobs                                # Create job posting (HR)
GET    /api/public/jobs                         # Public job listings
POST   /api/interview/screen/{candidateId}      # Trigger AI resume screening
POST   /api/interview/start/{candidateId}       # Start AI interview session
POST   /api/chatbot/ask                         # HR chatbot
POST   /api/feedback                            # Give peer feedback
POST   /api/complaints                          # Submit anonymous complaint
```

---

## Project Structure

```
src/main/java/com/PreetishRamola/hrms/
├── auth/           # JWT authentication
├── employee/       # Employee & user management
├── department/     # Department management
├── attendance/     # Attendance tracking
├── leave/          # Leave management
├── payroll/        # Payroll generation
├── performance/    # Performance reviews
├── recruitment/    # Job postings & candidate pipeline
├── ai/             # Groq AI: screening, interview, chatbot
├── feedback/       # Peer-to-peer feedback
├── complaint/      # Anonymous complaints
├── notification/   # Email notifications
└── config/         # Security, Redis, Cloudinary config
```
