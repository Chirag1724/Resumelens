# ARCHITECTURE.md
> **Authoritative reference for system structure, package layout, database schema, and REST endpoints.**
> All new code must stay consistent with this file. Do not introduce parallel or conflicting structures without first updating this document and logging the change in DECISIONS_LOG.md.

---

## System Architecture Overview

ResumeLens follows a classic three-tier architecture:

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT TIER                          │
│         React + Vite  (Single-Page Application)         │
│   Communicates via HTTP/JSON to the API tier            │
└────────────────────┬────────────────────────────────────┘
                     │  REST (JSON over HTTP)
┌────────────────────▼────────────────────────────────────┐
│                   API / LOGIC TIER                      │
│          Spring Boot REST API  (Java 17+)               │
│   Controllers → Services → Repositories (Spring JPA)   │
└────────────────────┬────────────────────────────────────┘
                     │  Spring Data JPA / Hibernate
┌────────────────────▼────────────────────────────────────┐
│                   DATA TIER                             │
│          PostgreSQL 15+ (hosted on Supabase)            │
└─────────────────────────────────────────────────────────┘
```

---

## Key Backend Libraries

These are the only third-party libraries authorised for the roles listed below. Do not swap them for alternatives without a corresponding entry in DECISIONS_LOG.md.

| Role | Library | Version target | Notes |
|------|---------|----------------|-------|
| PDF parsing | Apache PDFBox | 3.x | Extracts plain text from text-layer PDFs; used in `ResumeParserService` |
| DOCX parsing | Apache POI | 5.x | Extracts plain text from `.docx` files; used in `ResumeParserService` |
| PDF report generation | iText (Community) | 8.x | Generates downloadable PDF analysis reports; used in ReportGeneratorService |

All three are Apache-licensed or AGPL (iText Community) with no paid-API requirement, consistent with the no-paid-dependency principle in PROJECT_CONTEXT.md.

---

## Frontend Structure

**Technology:** React 18 + Vite + plain CSS (no Tailwind unless explicitly re-scoped)

```
frontend/
├── public/
├── src/
│   ├── api/               # Axios instance + per-feature API call modules
│   ├── assets/            # Static images, icons
│   ├── components/        # Reusable UI components (Button, Card, ScoreRing, etc.)
│   ├── pages/             # Page-level components (Home, Analyze, Results, History, Auth)
│   ├── hooks/             # Custom React hooks
│   ├── store/             # Lightweight state (Context API or Zustand — no Redux)
│   ├── utils/             # Pure helper functions (formatters, validators)
│   ├── App.jsx
│   └── main.jsx
├── index.html
└── vite.config.js
```

---

## Backend Package Structure

**Root package:** `com.resumelens`

```
backend/
└── src/main/java/com/resumelens/
    ├── ResumelensApplication.java          # Spring Boot entry point
    │
    ├── algorithm/                          # Self-implemented NLP algorithms
    │   ├── SimilarityCalculator.java       # Cosine similarity and Levenshtein fuzzy match
    │   └── TfIdfCalculator.java            # TF-IDF term weighting
    │
    ├── config/                             # Spring configuration classes
    │   └── CorsConfig.java
    │
    ├── controller/                         # HTTP layer only - no business logic
    │   ├── AnalysisController.java
    │   ├── AuthController.java
    │   ├── HealthController.java
    │   ├── HistoryController.java
    │   └── ResumeController.java
    │
    ├── dto/                                # API request/response objects
    │   ├── request/
    │   │   ├── AnalysisRequest.java
    │   │   └── AuthRequest.java
    │   └── response/
    │       ├── AnalysisResponse.java
    │       ├── ApiResponse.java            # Generic wrapper: {success, data, message}
    │       ├── AuthResponse.java
    │       ├── HistoryItemResponse.java
    │       ├── SectionScoreResponse.java
    │       └── SuggestionResponse.java
    │
    ├── entity/                             # JPA entities (never exposed via API)
    │   ├── JobDescription.java
    │   ├── MatchResult.java
    │   ├── Resume.java
    │   ├── SectionScore.java
    │   └── User.java
    │
    ├── exception/                          # Exception types + global handler
    │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
    │   ├── InvalidInputException.java
    │   ├── ResourceNotFoundException.java
    │   ├── ResumeParseException.java
    │   └── UnsupportedFileTypeException.java
    │
    ├── repository/                         # Spring Data JPA interfaces
    │   ├── JobDescriptionRepository.java
    │   ├── MatchResultRepository.java
    │   ├── ResumeRepository.java
    │   ├── SectionScoreRepository.java
    │   └── UserRepository.java
    │
    ├── security/                           # Spring Security and JWT authentication
    │   ├── JwtAuthFilter.java
    │   ├── JwtTokenProvider.java
    │   └── SecurityConfig.java
    │
    └── service/                            # All business logic lives here
        ├── AnalysisService.java
        ├── ATSScoreEngine.java
        ├── HistoryService.java
        ├── PromptBuilderService.java
        ├── ReportGeneratorService.java
        ├── ResumeParserService.java
        ├── ResumeService.java
        ├── SuggestionEngine.java
        └── UserService.java
```

---

## Database Schema

### Table: `users`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL |
| `password_hash` | VARCHAR(255) | NOT NULL |
| `name` | VARCHAR(100) | |
| `created_at` | DATETIME | DEFAULT NOW() |
| `updated_at` | DATETIME | |

---

### Table: `resumes`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `user_id` | BIGINT | FK → users.id, NULLABLE (guest submissions) |
| `raw_text` | TEXT | NOT NULL |
| `file_name` | VARCHAR(255) | |
| `uploaded_at` | DATETIME | DEFAULT NOW() |

---

### Table: `job_descriptions`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `user_id` | BIGINT | FK → users.id, NULLABLE |
| `title` | VARCHAR(255) | |
| `raw_text` | TEXT | NOT NULL |
| `created_at` | DATETIME | DEFAULT NOW() |

---

### Table: `match_results`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `user_id` | BIGINT | FK → users.id, NULLABLE |
| `resume_id` | BIGINT | FK → resumes.id |
| `job_description_id` | BIGINT | FK → job_descriptions.id |
| `overall_score` | DECIMAL(5,2) | NOT NULL |
| `ats_score` | DECIMAL(5,2) | |
| `skills_score` | DECIMAL(5,2) | |
| `impact_score` | DECIMAL(5,2) | |
| `project_score` | DECIMAL(5,2) | |
| `writing_score` | DECIMAL(5,2) | |
| `professionalism_score` | DECIMAL(5,2) | |
| `status_label` | VARCHAR(50) | e.g., "Strong Match", "Weak Match" |
| `improvement_prompt` | TEXT | Generated copyable prompt |
| `analyzed_at` | DATETIME | DEFAULT NOW() |

---

### Table: `section_scores`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `match_result_id` | BIGINT | FK → match_results.id |
| `section_name` | VARCHAR(100) | e.g., "Skills", "Experience" |
| `raw_score` | DECIMAL(5,2) | |
| `max_score` | DECIMAL(5,2) | |
| `feedback` | TEXT | Human-readable explanation |
| `matched_keywords` | TEXT | JSON array stored as string |
| `missing_keywords` | TEXT | JSON array stored as string |

---

## REST API Endpoints

> **Note:** For the full, authoritative request/response shapes, field types, and error status codes derived directly from live code, see **[API_CONTRACT.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/API_CONTRACT.md)**.
> For UI design specifications and product requirements, see **[FRONTEND_DESIGN.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/FRONTEND_DESIGN.md)** and **[REQUIREMENTS.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/REQUIREMENTS.md)**.

All endpoints are prefixed: `/api/v1`

### Resume Ingestion (`/api/v1/resumes`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/resumes/upload` | Optional (Guest/User) | Upload PDF/DOCX file (multipart). Returns generated `resumeId`. Links to user if JWT present. |

### Analysis & Reports (`/api/v1/analysis`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/analysis/match` | Public (Guest) | Matches resume against JD (`resumeId` + `rawJobDescription`). Returns full 6-dimension score, keywords, and suggestions. |
| `GET` | `/api/v1/analysis/{matchResultId}` | Public (Guest) | Retrieves previously computed match result by ID. |
| `GET` | `/api/v1/analysis/{matchResultId}/report` | Public (Guest) | Downloads generated iText PDF analysis report (`application/pdf`). |
| `GET` | `/api/v1/analysis/{matchResultId}/fix-prompt` | Public (Guest) | Generates copyable LaTeX improvement prompt for external AI assistant. |

### Authentication (`/api/v1/auth`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/auth/register` | Public (Guest) | Register a new user account with email, password, and optional name. Returns JWT. |
| `POST` | `/api/v1/auth/login` | Public (Guest) | Authenticate user credentials and receive JWT bearer token. |

### History (`/api/v1/history`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/history` | Authenticated | List all past analysis results for the logged-in user, ordered by date descending. |

### Health (`/api/v1/health`)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/health` | Public (Guest) | Returns `{ "status": "UP" }` for health/liveness probes. |

