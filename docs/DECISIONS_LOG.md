# DECISIONS_LOG.md
> **Running log of key technical and architectural decisions.**
> - Append new entries whenever a future prompt changes or adds to a prior decision.
> - Never rewrite or delete prior entries — amend them with a follow-up dated bullet.
> - Format: `YYYY-MM-DD — [Decision summary]`

---

## Foundational Decisions (Project Inception — 2026-09-13)

- **2026-09-13** — Chose **Spring Boot** (Java 17+) as the backend framework over Node.js/Express, Django, and FastAPI. Rationale: strong typing for algorithm implementation, mature Spring Data JPA for MySQL integration, Spring Security for optional auth, and team familiarity.

- **2026-09-13** — Chose **React 18 + Vite** as the frontend stack over Next.js. Rationale: no SSR or SEO requirement (tool is accessed directly by job-seekers, not indexed content), Vite's fast dev cycle, and simpler deployment as a static SPA. Next.js would add unnecessary complexity.

- **2026-09-13** — Chose **MySQL 8** with **Spring Data JPA / Hibernate** as the persistence layer. Rationale: relational structure fits the entities well (User → MatchResult → SectionScore), MySQL is widely hosted and familiar, and JPA reduces boilerplate CRUD.

- **2026-09-13** — Decided to **self-implement TF-IDF, Cosine Similarity, and Levenshtein Distance** in Java (under `com.resumelens.algorithm`) rather than using external NLP libraries (OpenNLP, Stanford NLP, Lucene, etc.). Rationale: keeps the dependency footprint minimal, avoids licence complexity, demonstrates algorithmic transparency, and all three algorithms are well within scope to implement correctly from scratch for this use case.

- **2026-09-13** — Decided that **resume improvement suggestions** are delivered as a **generated copyable text prompt** for the user to paste into an external AI assistant, not via an in-house LLM call. Rationale: avoids any paid API dependency, keeps the system fully functional offline-equivalent, and respects the "no AI API integration" architectural principle.

- **2026-09-13** — Explicitly **rejected a resume builder / template generation feature**. Rationale: different product surface requiring separate UX investment; out of scope for an ATS analysis tool.

- **2026-09-13** — Explicitly **rejected a chatbot / conversational assistant**. Rationale: replaced by the cleaner generated-prompt approach; a chatbot would require ongoing LLM API spend.

- **2026-09-13** — Explicitly **rejected RAG-based matching and vector database integration**. Rationale: the self-implemented TF-IDF + Cosine Similarity pipeline is sufficient and more transparent; RAG adds infrastructure overhead with no marginal benefit for this use case.

- **2026-09-13** — Explicitly **rejected campus/college-specific features** (e.g., internship-only scoring modes, college branding). Rationale: keeps the tool general-purpose and avoids fragmentation.

- **2026-09-13** — Decided to use **Flyway** for database schema migration management. Rationale: reproducible schema evolution, version-controlled migrations, avoids `spring.jpa.hibernate.ddl-auto=update` in production.

- **2026-09-13** — Decided on a **guest-first design**: all core features (analyze, score, view result) work without an account. Authentication only unlocks result history persistence. No feature should be gated behind login.

- **2026-09-13** — Decided to use the **`ApiResponse<T>` generic wrapper** for all REST responses to ensure a consistent `{ success, message, data }` envelope across the entire API surface.

- **2026-09-13** — Decided that **scoring weights are fixed** at: ATS 25%, Skills 25%, Impact 20%, Projects 15%, Writing 10%, Professionalism 5%. These values are documented in SCORING_RULES.md and must not be changed in code without a corresponding entry here.

---

- **2026-09-13** — Chose **Apache PDFBox 3.x** for PDF text extraction in `ResumeParserService`. Rationale: mature Apache-licensed library with reliable text-layer extraction, no paid dependency, well-supported in Java 17+ environments.

- **2026-09-13** — Chose **Apache POI 5.x** for DOCX text extraction in `ResumeParserService`. Rationale: the de facto standard for reading Microsoft Office formats in Java; Apache-licensed, no paid dependency.

- **2026-09-13** — Chose **iText Community (AGPL, 8.x)** for PDF report generation. Rationale: capable of producing structured, styled PDF output from Java without a paid licence under AGPL for an open-source project; encapsulated in a dedicated `ReportService` so the dependency is isolated and replaceable. If the project moves to a proprietary licence in future, iText's commercial licence or an alternative (e.g., OpenPDF) must be evaluated at that point.

*Append new entries below this line as decisions evolve.*

- **2026-09-22** — Removed fallback values from `DB_PASSWORD` and `JWT_SECRET` in `application.yml`. Previously `DB_PASSWORD` defaulted to an empty string and `JWT_SECRET` defaulted to a hardcoded placeholder string. Both are now required with no default, so the application fails fast at startup if either is missing rather than silently running with an insecure credential. `DB_URL` and `DB_USERNAME` retain their localhost defaults as neither is sensitive.

- **2026-09-22** — Chose **shell/IDE environment variable export** over adding a `spring-dotenv` (or equivalent) library dependency for `.env` file loading. Rationale: Spring Boot reads OS environment variables natively via the `${VAR}` placeholder syntax already in use; adding a dotenv library would introduce a new runtime dependency solely to replicate a capability the shell already provides. Decision aligns with the Ponytail principle of using the fewest external dependencies necessary. A `.env.example` file is committed at `backend/.env.example` as a reference template; the real `.env` is gitignored and must never be committed.

- **2026-10-10** — Changed `TfIdfCalculator.topKeywords` to compute IDF using a **sentence-level mini-corpus** rather than a single-document baseline. The input text is split on newline/sentence boundaries; each line becomes a separate document for IDF calculation. This ensures that terms appearing across many lines (e.g., "experience", "team") receive a low IDF and are down-weighted relative to terms that appear concentrated in one or two lines (e.g., "kubernetes", "react"). A single-document IDF gives every term the same smoothed value, making the ranking collapse to TF-only — the bug this change corrects. A constant `JD_FILLER` set of 18 generic JD terms is also applied as a post-rank filter to prevent common-but-uninformative JD boilerplate from occupying keyword slots.

- **2026-10-10** — Extended `TfIdfCalculator.topKeywords` sentence-split delimiter set to include **semicolons (`;`), bullet characters (`•`), en-dashes (`–`), and hyphens (`-`)** in addition to newlines and sentence-ending punctuation (`.!?`). Rationale: job descriptions pasted as a single paragraph (no newlines) would otherwise produce a one-document IDF corpus with no discrimination. With semicolons and bullets also acting as clause boundaries, each clause becomes a separate IDF document and rare technical terms receive higher IDF than ubiquitous filler. Bug exposed and fixed by the new `topKeywords_singleParagraphWithSemicolons_discriminatesRareTerms` test.

- **2026-10-10** — Added `@Transactional(readOnly = true)` to **`PromptBuilderService.buildFixPrompt`**. Bug: the method accesses `result.getResume().getRawText()` and `result.getJobDescription().getDescription()` which are LAZY-fetched relations. When called from `AnalysisController.getFixPrompt` (outside a transaction), these accesses throw `LazyInitializationException`. The fix opens a read-only transaction for the duration of the method. Found during integration test development for `AnalysisControllerIT`.

- **2026-10-10** — Added `@Transactional(readOnly = true)` to **`HistoryService.getHistoryForUser`**. Bug: the method maps over `MatchResult` entities and accesses `mr.getResume().getId()`, `mr.getResume().getFileName()`, and `mr.getJobDescription().getTitle()` — all LAZY-fetched relations. Without a transaction, these accesses throw `LazyInitializationException` when called from `HistoryController`. The fix opens a read-only transaction for the duration of the method. Found during integration test development for `HistoryControllerIT`.

- **2026-10-15** — Authored authoritative REST API contract and UI design specifications (`docs/API_CONTRACT.md`, `docs/FRONTEND_DESIGN.md`, and `docs/REQUIREMENTS.md`). Replaced the preliminary REST endpoint overview in `docs/ARCHITECTURE.md` with explicit cross-links to `docs/API_CONTRACT.md` reflecting the live controller and DTO implementation (`/resumes/upload`, `/analysis/match`, `/analysis/{id}/report`, `/analysis/{id}/fix-prompt`, `/auth/register`, `/auth/login`, and `/history`). Aligned the frontend status label thresholds and color tokens in `docs/FRONTEND_DESIGN.md` strictly to the canonical 85, 70, 55, 40 boundary scale defined in `docs/SCORING_RULES.md`, adopting a dark charcoal dashboard aesthetic with burnt orange primary accent, line iconography, and strict restriction of crimson red to critical failures (< 40 and high-severity risks).

- **2026-10-22** - Implemented IDOR fix using unguessable UUID public IDs for Resume and MatchResult entities. Numeric auto-increment IDs remain internal to database tables and are never exposed in REST responses or URLs. The upload response returns the resume publicId as resumeId. Guest access remains fully functional under the "anyone with the link can view" capability model: possession of the unguessable MatchResult UUID grants read access to the analysis result, fix prompt, and PDF report without requiring user login.

- **2026-10-22** - Implemented file signature validation for resume uploads. Verified magic bytes: PDF files must start with %PDF- and DOCX files must start with PK 0x03 0x04. Mismatches throw UnsupportedFileTypeException (HTTP 415). Configured 10MB multipart size limits in application.yml and handled MaxUploadSizeExceededException with HTTP 413. Caught RuntimeException in extractPdf and extractDocx to rethrow ResumeParseException (HTTP 422).

- **2026-10-22** - Configured unified AuthenticationEntryPoint and AccessDeniedHandler in SecurityConfig returning standard ApiResponse JSON envelopes with HTTP 401 and 403 respectively, eliminating inconsistent anonymous access statuses.

- **2026-10-22** - Normalized string casing across algorithm and parser services (ResumeParserService, ATSScoreEngine, SuggestionEngine, TfIdfCalculator, SimilarityCalculator) by explicitly passing Locale.ROOT to all toLowerCase and toUpperCase calls.

- **2026-10-22** - Standardized error hygiene across the API surface. ResourceNotFoundException returns a generic message ("The requested resource was not found.") without leaking internal entity class names or identifiers. Added SLF4J logger to GlobalExceptionHandler logging full exceptions in the generic handler only, ensuring no resume texts, emails, or request bodies are logged.

- **2026-10-22** - Implemented embedded graphics detection at upload time in ResumeParserService for PDF (image XObjects across pages) and DOCX (embedded pictures via getAllPictures). Added hasGraphicsRisk boolean flag to Resume entity (default false) persisted during upload and consumed by AnalysisService instead of hardcoded false.

- **2026-10-22** - Updated docs/SCORING_RULES.md to align with implementation: clarified that the measurable achievements rule checks the whole resume text for digits or percentages, and replaced the -20 non-parseable PDF rule with upload rejection via HTTP 422 (ResumeParseException) with no score generated.

- **2026-10-22** - Consolidated status threshold evaluation across the application into a single shared method (SectionScore.deriveStatus) based on canonical thresholds 85, 70, 55, 40. Updated SectionScore.status to EXCELLENT, STRONG, MODERATE, WEAK, POOR, and eliminated separate 4-tier logic in SectionScore and hardcoded 70/40 color cutoffs in ReportGeneratorService.

- **2026-10-22** - Configured history endpoint to return at most the latest 50 results using PageRequest.of(0, 50). Optimized MatchResultRepository query with @EntityGraph eagerly fetching resume and jobDescription to eliminate N+1 queries while preserving the response shape.

- **2026-10-22** - Standardized MySQL database name to resumelens across default configuration (application.yml), environment templates (.env.example), documentation, and README.

- **2026-10-22** - Generated platform-independent Maven wrappers (mvnw and mvnw.cmd) inside backend/. Updated README commands to use wrappers explicitly (./mvnw on Unix, .\mvnw.cmd on Windows) without bare mvn, and marked frontend setup as coming soon.

- **2026-10-22** - Rewrote the backend package structure in docs/ARCHITECTURE.md to reflect the exact current codebase, removing obsolete class references (ScoringService, PromptGeneratorService, JwtConfig, util/*, separate Cosine and Levenshtein classes, ActionVerbList).
- **2026-10-24** - Replaced range-style HTTP status assertions across integration test controllers (AuthControllerIT, HistoryControllerIT, AnalysisControllerIT, ResumeControllerIT) with exact status code assertions (400 for duplicate email and bad requests, 401 for bad credentials and missing history token, 404 for missing resources, 413 for size limit, 415 for invalid file type, and 422 for unparseable or empty extractable text). Standardized error body assertions across all integration test error paths to verify $.success is false and $.message is non-empty.

- **2026-10-24** - Added scoring and algorithm boundary tests in ATSScoreEngineTest and SimilarityCalculatorTest using hardcoded expected literals computed strictly from docs/SCORING_RULES.md rather than production constants: verified all WEIGHT constants match the specification and sum to 1.0; evaluated a fixed resume and JD fixture against a hardcoded expected overall score; checked keyword density deduction boundaries at 50% (0), 30% (-8), and below 30% (-15); tested isFuzzyMatch equality at threshold and threshold plus 1; verified floors at 0 for worst-case ATS, Writing, and Professionalism dimensions; validated skills match ratio with cosine similarity bonus and 100 ceiling cap; validated 2 of 4 (50), 4 of 4 (100), and two-project average (75) for project quality; verified graphics deduction of exactly 8 points; and validated 422 response for uploads with no extractable text.

- **2026-10-24** - Conducted manual mutation verification for four critical paths: WEIGHT_ATS changed to 0.35, keyword density threshold changed to matchPct <= 0.30, isFuzzyMatch distance comparison changed to strict inequality (< threshold), and ResourceNotFoundException status changed to HTTP 400. Verified each mutation causes test failures in the corresponding test suite and restored all mutated files by hand without git checkout/reset.

- **2026-10-24** - Verified docs/FRONTEND_DESIGN.md and docs/REQUIREMENTS.md align with canonical status tiers (EXCELLENT, STRONG, MODERATE, WEAK, POOR) and threshold boundaries (85, 70, 55, 40), ensuring no legacy labels or obsolete thresholds remain in project specifications.

- **2026-10-25** - Switched the persistence layer from MySQL 8 to PostgreSQL 15, hosted on Supabase (used purely as a managed PostgreSQL provider; no Supabase Auth, Storage, or client SDK is added). Rationale: Supabase provides a free-tier hosted PostgreSQL with SSL and connection pooling that eliminates the need for a self-managed database server for cloud deployment, while keeping the Spring Boot / Spring Data JPA / Hibernate stack identical. Changes made: (1) Replaced `com.mysql:mysql-connector-j` with `org.postgresql:postgresql` (runtime scope) in `backend/pom.xml`; H2 test dependency unchanged. (2) Replaced `columnDefinition = "LONGTEXT"` with `columnDefinition = "TEXT"` on `Resume.rawText`, `JobDescription.description`, and `MatchResult.improvementPrompt`; TEXT is standard SQL supported by PostgreSQL, H2 (PostgreSQL mode), and Hibernate schema generation. (3) Updated `application.yml` default datasource URL to `jdbc:postgresql://localhost:5432/resumelens`, changed default username to `postgres`, and removed the explicit `driver-class-name` and `hibernate.dialect` properties so Hibernate auto-detects both PostgreSQL in production and H2 in tests. (4) Updated `application-test.yml` H2 URL MODE from MySQL to PostgreSQL so the in-memory schema matches production column types. (5) Updated `backend/.env.example` with a Supabase Session pooler example URL (`jdbc:postgresql://HOST_FROM_DASHBOARD:5432/postgres?sslmode=require`) and Session pooler username format (`postgres.YOUR_PROJECT_REF`). (6) Updated README.md, docs/ARCHITECTURE.md, docs/CONVENTIONS.md, docs/PROJECT_CONTEXT.md, and docs/REQUIREMENTS.md to remove MySQL references and reflect PostgreSQL on Supabase.

- **2026-10-25** - Deterministic suggestion recomputation for GET /api/v1/analysis/{id}. AnalysisService.getResult returned an empty suggestions list (List.of()) on retrieval. Updated getResult to recompute suggestions deterministically from the persisted resume text and job description using SuggestionEngine (findMissingKeywords, detectWeakVerbs, checkQuantification, and generateTopSuggestions) alongside overall score, with read-only transaction semantics (@Transactional(readOnly = true)) to cleanly initialize lazy entity associations. Refactored AnalysisService.runAnalysis to share the same deterministic computeSuggestions helper method, ensuring GET /api/v1/analysis/{id} returns identical suggestions as POST /api/v1/analysis/match without modifying the response schema or introducing new database tables. Added an integration test in AnalysisControllerIT asserting GET suggestions equal POST suggestions for the same result.

- **2026-10-25** — Added scoring risks list to the analysis response (`AnalysisResponse.risks`) derived strictly from fired deduction rules in `ATSScoreEngine` across ATS Compatibility, Writing Quality, and Professionalism dimensions without changing any score value or formula. Each risk item carries `title`, `severity` (`HIGH`, `MEDIUM`, `LOW`), `description`, and `fix`. Severity is derived strictly from the deduction size incurred by the rule: 15 or more points is `HIGH`, 8 to 14 points is `MEDIUM`, and below 8 points is `LOW`. All risk definitions (title, description, fix) are defined in one central constant map (`RISK_DEFINITIONS`) using short plain sentences without dynamic or invented numbers. Risks are ordered by severity (`HIGH` > `MEDIUM` > `LOW`) while preserving firing order within the same tier. Added a `hasFormattingRisk` boolean column to `Resume` (persisted at upload time via `ResumeService` and detected via `ResumeParserService.detectFormattingRisks`) matching the existing pattern for `hasGraphicsRisk`. `GET /api/v1/analysis/{id}` recomputes risks deterministically from the persisted resume text, job description, and persisted risk flags using `ATSScoreEngine`, ensuring `POST /match` and `GET /analysis/{id}` return identical risks for the same result. Added test cases in `AnalysisControllerIT` and `ATSScoreEngineTest` verifying that GET risks equal POST risks for the same result, a clean resume produces an empty risks list, and a resume lacking contact info produces a MEDIUM severity risk with non-empty description and fix.


