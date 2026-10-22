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
