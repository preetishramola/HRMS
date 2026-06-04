# HRMS Backend

AI-Powered Human Resource Management System built with Spring Boot 3.3, Spring AI, PostgreSQL, Redis, and Cloudinary.

## 🚀 Quick Start

### 1. Prerequisites
- Java 21
- Docker Desktop
- IntelliJ IDEA

### 2. Clone & Setup
```bash
git clone <your-repo>
cd hrms-backend
cp .env.example .env
# Fill in your API keys in .env
```

### 3. Start Infrastructure
```bash
docker-compose up -d
```

### 4. Run Application
Open in IntelliJ → Run `HrmsApplication.java`

OR via CLI:
```bash
./mvnw spring-boot:run
```

### 5. Verify Running
```bash
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hrms.com","password":"admin@123"}'
```

## 🔑 Demo Credentials

| Role     | Email                  | Password      |
|----------|------------------------|---------------|
| Admin    | admin@hrms.com         | admin@123     |
| Manager  | manager@hrms.com       | manager@123   |
| HR       | hr@hrms.com            | hr@123        |
| Employee | employee@hrms.com      | employee@123  |

## 📦 Tech Stack
- Spring Boot 3.3.5 + Java 21
- Spring Security + JWT
- Spring AI (Groq - Llama 3.3 70B)
- Gemini Flash (Resume Screening)
- PostgreSQL 16 + Redis 7
- Cloudinary (Resume Storage)
- Docker Compose

## 🤖 AI Features
- **Resume Screening**: POST /api/interview/screen/{candidateId}
- **Voice Interview**: POST /api/interview/start/{candidateId}
- **HR Chatbot**: POST /api/chatbot/ask?question=...
