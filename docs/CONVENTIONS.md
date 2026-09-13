# CONVENTIONS.md
> **Style guide and coding conventions for the ResumeLens codebase.**
> All generated code — backend and frontend — must follow these rules. Deviations are bugs, not preferences.

---

## Backend (Java / Spring Boot)

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Classes | `UpperCamelCase` | `ScoringService`, `AnalysisController` |
| Interfaces | `UpperCamelCase` (no `I` prefix) | `UserRepository` |
| Methods | `lowerCamelCase`, verb-first | `calculateScore()`, `findByEmail()` |
| Variables / fields | `lowerCamelCase` | `matchedSkills`, `overallScore` |
| Constants | `UPPER_SNAKE_CASE` | `MAX_KEYWORD_COUNT`, `LEVENSHTEIN_THRESHOLD` |
| Packages | `lowercase`, dot-separated | `com.resumelens.algorithm` |
| JPA entities | Singular noun | `User`, `MatchResult` |
| Database tables | `snake_case`, plural | `match_results`, `section_scores` |
| DB columns | `snake_case` | `user_id`, `overall_score` |
| DTO classes | Suffix `Request` or `Response` | `AnalysisRequest`, `AnalysisResponse` |
| Exception classes | Suffix `Exception` | `InvalidInputException` |

### REST Endpoint Conventions

- All paths: **lowercase kebab-case**, plural nouns for resources.
  - ✅ `/api/v1/match-results/{id}`
  - ❌ `/api/v1/matchResults/{id}`
- Version prefix: `/api/v1/` — always present, never omitted.
- HTTP verbs map strictly:
  - `GET` → read (no side effects)
  - `POST` → create or trigger an action (analysis)
  - `PUT` → full replacement update
  - `PATCH` → partial update
  - `DELETE` → remove
- Query params for filtering, path params for identity (`/history/{id}`).

### Controller Pattern

Controllers must be **thin**. They are responsible for:
1. Accepting and deserializing the HTTP request.
2. Delegating to a service method.
3. Wrapping the result in `ApiResponse<T>` and returning with the correct HTTP status.

Controllers must **not** contain:
- Any business logic or algorithmic computation.
- Direct repository calls.
- Conditional scoring or string manipulation.

```java
// ✅ Correct controller method
@PostMapping("/analysis")
public ResponseEntity<ApiResponse<AnalysisResponse>> analyze(
        @RequestBody AnalysisRequest request) {
    AnalysisResponse result = analysisService.analyze(request);
    return ResponseEntity.ok(ApiResponse.success(result));
}
```

### Service Layer Pattern

- One service per domain area (analysis, auth, history, scoring, etc.).
- Services may call other services (e.g., `AnalysisService` → `ScoringService`).
- Services must **not** call controllers.
- Algorithm classes (`TfIdfCalculator`, etc.) are stateless utilities — inject as `@Component` and call their pure methods.

### Exception Handling Pattern

Use a single `GlobalExceptionHandler` annotated with `@RestControllerAdvice`.

**Always throw typed exceptions; never swallow them silently.**

```java
// Throw
throw new ResourceNotFoundException("MatchResult", id);
throw new InvalidInputException("Resume text cannot be empty");

// GlobalExceptionHandler maps these to:
// 404 ResourceNotFoundException  → { success: false, message: "...", data: null }
// 400 InvalidInputException      → { success: false, message: "...", data: null }
// 500 RuntimeException (default) → { success: false, message: "Internal server error", data: null }
```

Never return raw stack traces to the client.

### DTO Pattern

- **Request DTOs** live in `dto/request/` and use Bean Validation annotations (`@NotBlank`, `@Size`, etc.).
- **Response DTOs** live in `dto/response/` and are plain POJOs (or Java records).
- The generic wrapper for all API responses:

```java
public record ApiResponse<T>(boolean success, String message, T data) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data);
    }
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
```

- JPA entities (`entity/` package) must **never** appear in controller method signatures or return types.

### Other Backend Rules

- Use **constructor injection** (not field injection with `@Autowired`).
- Prefer Java **records** for immutable response DTOs.
- All `@Entity` classes must have a no-arg constructor (JPA requirement).
- Database migrations are handled via **Flyway** scripts in `src/main/resources/db/migration/`, named `V{n}__{description}.sql`.
- Unit tests go in `src/test/java/`, mirroring the main package structure. Test classes are named `{ClassName}Test`.

---

## Frontend (React / Vite)

### Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Component files | `PascalCase.jsx` | `ScoreRing.jsx`, `ResultsPage.jsx` |
| Hook files | `use{Name}.js` | `useAnalysis.js` |
| Utility files | `camelCase.js` | `formatScore.js` |
| CSS files | Same name as component | `ScoreRing.css` |
| API modules | `camelCase.js` in `src/api/` | `analysisApi.js` |
| Constants | `UPPER_SNAKE_CASE` in `src/utils/constants.js` | `API_BASE_URL` |

### Component Rules

- One component per file.
- Components must be **pure and presentational** where possible; data-fetching logic lives in custom hooks or page-level components.
- Prop-types or JSDoc comments required for all non-trivial components.
- No inline styles — all styling via CSS classes defined in the component's `.css` file or `index.css`.

### API Call Pattern

```javascript
// src/api/analysisApi.js
import axiosInstance from './axiosInstance';

export const submitAnalysis = (formData) =>
  axiosInstance.post('/analysis', formData);
```

- All API calls go through the shared `axiosInstance` (base URL, auth header injection, error interceptor configured centrally).
- No raw `fetch()` calls; always use Axios.

### Error Handling (Frontend)

- API errors are caught in the calling hook and surfaced to the user via a consistent error state (not console.log).
- Never show raw server error messages to the user — map them to friendly strings.

---

## General Rules (Both Tiers)

- **No magic numbers.** Extract numeric constants (thresholds, weights, limits) to named constants with explanatory comments.
- **No commented-out code** in committed files. Remove dead code; use version control for history.
- **English only** for all identifiers, comments, and commit messages.
- **No TODO left untracked** — every TODO comment must reference a GitHub issue or be removed.
