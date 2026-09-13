# PROJECT_CONTEXT.md
> **Single source of truth for project identity, scope, and principles.**
> Read this file before generating or modifying any code. If a later instruction conflicts with anything here, flag the conflict instead of silently overriding it.

---

## Project Summary

ResumeLens is a web-based system that evaluates a candidate's resume against a target job description and predicts ATS (Applicant Tracking System) compatibility. It accepts a resume (text or PDF) and a job description as input, then scores and analyses the match using self-implemented text-processing algorithms — TF-IDF for keyword weighting, Cosine Similarity for semantic relevance measurement, and Levenshtein Distance for fuzzy skill matching. The backend is built with Spring Boot and exposes a REST API consumed by a React (Vite) single-page frontend, with all persistent data stored in a MySQL database. The system produces a structured multi-dimensional score, a section-by-section breakdown, and a copyable AI-prompt that users can take to an external assistant to get resume improvement advice — the system itself never calls any AI or LLM API.

---

## Strict Scope Boundary

The following features are **explicitly out of scope** and must **never** be added without a conscious, documented scope change. If any future instruction implies one of these, **flag it** rather than implementing it silently:

| # | Out-of-Scope Feature | Reason flagged |
|---|----------------------|----------------|
| 1 | **Resume builder / template generation** | Different product surface; outside this project's mission |
| 2 | **In-house LLM / AI API integration** | Adds paid dependency and architectural complexity |
| 3 | **RAG (Retrieval-Augmented Generation) or vector database** | Unnecessary given self-implemented similarity algorithms |
| 4 | **Live job portal integration** (LinkedIn, Indeed, etc.) | Requires third-party API agreements and rate-limit handling |
| 5 | **Recruiter-facing dashboard** | This product is candidate-facing only |
| 6 | **Multi-job comparison feature** | Out of core scope; deferred unless explicitly re-scoped |
| 7 | **Chatbot / conversational assistant** | Replaced by the generated-prompt approach |
| 8 | **Campus / college-specific features** | Keep the tool general-purpose |

---

## Architectural Principles

1. **Self-implemented algorithms.** All core scoring logic (TF-IDF, Cosine Similarity, Levenshtein Distance, ATS rule checks) must be implemented in Java within the Spring Boot service layer. Do not delegate these to any external NLP, ML, or AI library (e.g., no OpenNLP, Stanford NLP, Apache Solr, or Elasticsearch for core matching).

2. **No paid API dependencies.** No feature, at any point in the project, should require a paid third-party API key or subscription to function.

3. **Guest-first design.** Login is optional everywhere. Guest usage — uploading a resume, entering a job description, and receiving a full score — must always remain fully functional without an account. Authenticated sessions only add persistence (saved history); they never gate core features.

4. **AI-prompt pattern for improvement suggestions.** The resume-improvement feature works by generating a structured text prompt that the user copies and pastes into an external AI assistant (e.g., ChatGPT, Gemini) manually. The system itself never calls an AI API. Any code that attempts to call an external AI endpoint violates this principle and must be flagged.

5. **Thin controllers, rich services.** Controllers must only handle HTTP parsing, validation delegation, and response wrapping. All business logic lives in the service layer.

6. **DTO boundary.** JPA entities must never be returned directly from REST controllers. All API responses use dedicated DTO/response objects.
