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
