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
│                    MySQL 8+                             │
└─────────────────────────────────────────────────────────┘
```

---

## Key Backend Libraries

These are the only third-party libraries authorised for the roles listed below. Do not swap them for alternatives without a corresponding entry in DECISIONS_LOG.md.

| Role | Library | Version target | Notes |
|------|---------|----------------|-------|
| PDF parsing | Apache PDFBox | 3.x | Extracts plain text from text-layer PDFs; used in `ResumeParserService` |
| DOCX parsing | Apache POI | 5.x | Extracts plain text from `.docx` files; used in `ResumeParserService` |
| PDF report generation | iText (Community) | 8.x | Generates downloadable PDF analysis reports; used in a dedicated `ReportService` |

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
    ├── controller/                         # HTTP layer only — no business logic
    │   ├── AnalysisController.java
    │   ├── AuthController.java
    │   ├── HistoryController.java
    │   └── HealthController.java
    │
    ├── service/                            # All business logic lives here
    │   ├── AnalysisService.java
    │   ├── ScoringService.java
    │   ├── ResumeParserService.java
    │   ├── PromptGeneratorService.java
    │   ├── UserService.java
    │   └── HistoryService.java
    │
    ├── algorithm/                          # Self-implemented NLP algorithms
    │   ├── TfIdfCalculator.java
    │   ├── CosineSimilarityCalculator.java
    │   └── LevenshteinDistance.java
    │
    ├── repository/                         # Spring Data JPA interfaces
    │   ├── UserRepository.java
    │   ├── ResumeRepository.java
    │   ├── JobDescriptionRepository.java
    │   ├── MatchResultRepository.java
    │   └── SectionScoreRepository.java
    │
    ├── entity/                             # JPA entities (never exposed via API)
    │   ├── User.java
    │   ├── Resume.java
    │   ├── JobDescription.java
    │   ├── MatchResult.java
    │   └── SectionScore.java
    │
    ├── dto/                                # API request/response objects
    │   ├── request/
    │   │   ├── AnalysisRequest.java
    │   │   └── AuthRequest.java
    │   └── response/
    │       ├── AnalysisResponse.java
    │       ├── SectionScoreResponse.java
    │       ├── HistoryItemResponse.java
    │       └── ApiResponse.java            # Generic wrapper: {success, data, message}
    │
    ├── exception/                          # Exception types + global handler
    │   ├── ResourceNotFoundException.java
    │   ├── InvalidInputException.java
    │   └── GlobalExceptionHandler.java     # @RestControllerAdvice
    │
    ├── config/                             # Spring configuration classes
    │   ├── SecurityConfig.java
    │   ├── CorsConfig.java
    │   └── JwtConfig.java
    │
    └── util/                               # Pure utility helpers (text normalisation, etc.)
        ├── TextNormalizer.java
        └── JwtUtil.java
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
| `raw_text` | LONGTEXT | NOT NULL |
| `file_name` | VARCHAR(255) | |
| `uploaded_at` | DATETIME | DEFAULT NOW() |

---

### Table: `job_descriptions`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT |
| `user_id` | BIGINT | FK → users.id, NULLABLE |
| `title` | VARCHAR(255) | |
| `raw_text` | LONGTEXT | NOT NULL |
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
| `improvement_prompt` | LONGTEXT | Generated copyable prompt |
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

All endpoints are prefixed: `/api/v1`

### Analysis

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/analysis` | Optional | Submit resume + JD; receive full scored result. Multipart form (file + JD text) or JSON body with raw text. |
| `GET` | `/api/v1/analysis/{resultId}` | Optional | Retrieve a previously stored analysis result by ID. |

### Authentication

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/auth/register` | None | Register a new user account. |
| `POST` | `/api/v1/auth/login` | None | Authenticate and receive a JWT token. |
| `POST` | `/api/v1/auth/logout` | Required | Invalidate / blacklist the current token. |

### History (authenticated users only)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/history` | Required | List all past analysis results for the logged-in user. |
| `GET` | `/api/v1/history/{resultId}` | Required | Fetch a specific historical result. |
| `DELETE` | `/api/v1/history/{resultId}` | Required | Delete a specific historical result. |

### Health

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/health` | None | Returns `{ "status": "UP" }` for liveness checks. |
