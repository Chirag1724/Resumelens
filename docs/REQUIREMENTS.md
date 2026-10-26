# REQUIREMENTS.md
> **Product and technical requirements specification for ResumeLens.**
> Authoritative scope, feature prioritisation, and acceptance criteria.
> Linked to [API_CONTRACT.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/API_CONTRACT.md), [SCORING_RULES.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/SCORING_RULES.md), and [FRONTEND_DESIGN.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/FRONTEND_DESIGN.md).

---

## 1. Product Mission & Scope

ResumeLens provides job-seekers with an objective, fast, and transparent evaluation of their resume against target job descriptions. The system identifies ATS formatting bottlenecks, analyzes technical skill overlap, detects unquantified impact bullets, and produces copyable AI-ready LaTeX prompts without charging fees or relying on third-party AI APIs.

### 1.1 User Personas
1. **Guest Job-Seeker (Primary):** Wants immediate feedback on a resume for a specific job application without the friction of signing up or disclosing personal identity.
2. **Registered Job-Seeker (Secondary):** Wants to track score improvements across iterations, compare matches for different roles, and maintain a longitudinal history.

---

## 2. Functional Requirements

### 2.1 Must-Have Requirements (Core Scope)

#### FR-1: Frictionless Guest Flow
- The application must allow users to upload resumes, enter job descriptions, and view comprehensive score results completely anonymously.
- No feature on the analysis or report page shall be locked behind authentication.
- A user can optionally create an account or sign in at any point.

#### FR-2: Document Ingestion & Parsing
- The system must accept resume uploads in **PDF** (`.pdf`) and **Microsoft Word** (`.docx`) formats.
- The system must extract clean text layers using Apache PDFBox (PDF) and Apache POI (DOCX).
- Maximum allowed file size is strictly **10 MB**. Files exceeding this limit or presenting unsupported MIME types must be rejected with user-friendly error messages (HTTP 400 / 415).
- If a PDF contains no extractable text layer (scanned bitmap/image), the system must flag an unparseable document error (HTTP 422).

#### FR-3: Six-Dimension Score Calculation
The system must calculate and return scores (0.00 – 100.00) across six discrete dimensions strictly weighted as defined in [SCORING_RULES.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/SCORING_RULES.md):
1. **ATS Compatibility (25%):** Penalties for missing standard headers (Education, Experience, Skills), absent contact info, tables/columns, graphics, and low keyword density.
2. **Skills Validation (25%):** TF-IDF top keyword extraction from the JD, exact matching, Levenshtein fuzzy distance matching (distance ≤ 2), and Cosine Similarity bonus vector calculations.
3. **Impact & Achievements (20%):** Ratio of bullet points containing numeric metrics, percentages, and strong action verbs.
4. **Project Quality (15%):** Assessment of project sections for titles, tech stack mentions, descriptions, and measurable outcomes.
5. **Writing Quality (10%):** Sentence length analysis, passive voice ratio, first-person pronoun detection, and filler word deductions.
6. **Professionalism (5%):** Page length estimation (> 2 pages penalized), date formatting consistency, bullet style uniformity, and email address professionalism.
- Overall score must be an exact weighted combination rounded to two decimal places.
- Qualitative status labels must be assigned by exact thresholds:
  - `≥ 85.00`: **Excellent Match**
  - `70.00 – 84.99`: **Strong Match**
  - `55.00 – 69.99`: **Moderate Match**
  - `40.00 – 54.99`: **Weak Match**
  - `< 40.00`: **Poor Match**

#### FR-4: Keyword Analysis & Missing Skills
- The system must display all extracted keywords from the JD.
- Keywords must be classified into:
  - **Matched Skills:** Present in resume via exact or fuzzy match.
  - **Missing Skills:** Absent from resume; prioritized for resume revision.
- Overall keyword match percentage must be displayed.

#### FR-5: ATS Risk Detection
- The system must identify and highlight specific ATS failure risks:
  - Missing essential headings (Education, Experience, Skills).
  - Missing contact details (email or phone).
  - Layout risks (multi-column layouts, tables).
  - Graphic risks (images, non-standard embedded elements).
  - Resume length overflow (> 2 pages / > 700 words).

#### FR-6: Actionable Recommendations
- The system must generate prioritized recommendations (`HIGH`, `MEDIUM`, `LOW`):
  - Missing critical keywords.
  - Action verb replacements for detected weak verbs.
  - Unquantified bullets requiring metrics or percentages.

#### FR-7: Copyable AI Fix-Prompt Generation
- The system must generate a structured, copyable prompt designed for pasting into external LLMs (ChatGPT, Claude, Gemini).
- The prompt must include:
  - Target job title.
  - Full candidate resume text and target JD text.
  - Missing keywords and weak verbs to replace.
  - Strict anti-fabrication guidelines (no hallucinated dates, companies, or fake metrics).
  - LaTeX template formatting instruction (`Simple Single-Column`, `Modern Clean`, or `Compact Academic`).
- The frontend must provide a one-click copy button with clipboard confirmation.

#### FR-8: Downloadable PDF Analysis Report
- The system must allow users to download an official PDF report via `GET /api/v1/analysis/{matchResultId}/report`.
- The PDF must be generated server-side using iText 8 with clean typography, dimension breakdown table, matched/missing keyword summaries, and overall score badges.

---

### 2.2 Optional Requirements (Authenticated Scope)

#### FR-9: User Authentication & Account Management
- Registration (`POST /api/v1/auth/register`) with email, password (min 8 chars), and name.
- Authentication (`POST /api/v1/auth/login`) issuing stateless JWT tokens with 24-hour expiration.
- Auto-linking of uploads to authenticated user when Bearer token is present.

#### FR-10: Analysis History & Longitudinal Tracking
- Authenticated endpoint `GET /api/v1/history` returning all previous analyses for the logged-in user in descending chronological order.
- History dashboard page displaying past submissions, target job titles, dates, and scores.
- Re-opening any historical report via direct link.

#### FR-11: Score Trend Visualization
- Interactive graphical visualization showing historical score progression over successive resume revisions for authenticated users.

---

## 3. Non-Functional Requirements (NFR)

### 3.1 Performance & Latency
- **Analysis Execution Time:** End-to-end resume parsing, TF-IDF calculation, and scoring must complete in `< 3.0 seconds` for a standard 2-page document.
- **Report Generation:** Server-side PDF generation must complete in `< 1.5 seconds`.
- **Frontend Load Time:** Initial Vite SPA bundle load under `< 1.0 second` on broadband connections.

### 3.2 File Constraints
- **Supported File Types:** `.pdf` and `.docx` only. All other extensions (`.txt`, `.rtf`, `.png`, `.jpg`) must be rejected at both client and server boundaries.
- **File Size Limit:** Strict ceiling of **10 MB**.

### 3.3 Browser Compatibility
- Modern evergreen desktop and mobile browsers:
  - Google Chrome (latest 2 versions)
  - Mozilla Firefox (latest 2 versions)
  - Apple Safari (latest 2 versions)
  - Microsoft Edge (latest 2 versions)
- Responsive design adapting smoothly down to 375px mobile viewports.

### 3.4 Security & Data Privacy
- **Stateless Authentication:** JWT signed with HMAC-SHA256, verified on each request.
- **Password Hashing:** Passwords hashed using standard BCrypt with salt rounds ≥ 10.
- **Zero Information Leakage:** Stack traces and internal database errors are swallowed by `GlobalExceptionHandler` and replaced with standard messages.
- **No Third-Party AI Data Sharing:** Resume text is never dispatched to any external cloud AI provider.

### 3.5 Architectural Constraints
- **No Paid API Dependency:** All algorithms (TF-IDF, Levenshtein, Cosine Similarity) are implemented natively in Java.
- **Database Portability:** Managed with Flyway migrations on PostgreSQL 15 (Supabase); compatible with H2 in-memory test profiles.
