# REQUIREMENTS.md
> Product and technical requirements specification for ResumeLens.
> Authoritative scope, feature prioritisation, and acceptance criteria.
> Linked to [API_CONTRACT.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/API_CONTRACT.md), [SCORING_RULES.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/SCORING_RULES.md), and [FRONTEND_DESIGN.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/FRONTEND_DESIGN.md).

---

## 1. Product Mission and Scope

ResumeLens provides job-seekers with an objective, fast, and transparent evaluation of their resume against target job descriptions. The system identifies ATS formatting bottlenecks, analyzes technical skill overlap, detects unquantified impact bullets, and produces copyable AI-ready improvement prompts without charging fees or relying on third-party AI APIs.

### 1.1 User Personas
1. **Guest Job-Seeker (Primary)**: Wants immediate feedback on a resume for a specific job application without the friction of signing up or disclosing personal identity.
2. **Registered Job-Seeker (Secondary)**: Wants to track score progression across iterations, compare matches for different roles, and maintain a history.

---

## 2. Functional Requirements

### 2.1 Core Scope

#### FR-1: Frictionless Guest Flow
- The application allows users to upload resumes, enter job descriptions, and view comprehensive score results completely anonymously.
- No feature on the analysis or report page is locked behind authentication.
- Authentication only unlocks result history persistence.
- A user can optionally register or log in at any point.

#### FR-2: Document Ingestion and Parsing
- The system accepts resume uploads in PDF (`.pdf`) and Microsoft Word (`.docx`) formats.
- The system extracts clean text layers using Apache PDFBox (PDF) and Apache POI (DOCX).
- Maximum allowed file size is strictly 10 MB. Files exceeding this limit or presenting unsupported file signatures are rejected with clear error messages (HTTP 413, HTTP 415).
- If a document contains no extractable text layer (such as a scanned image PDF), the upload is rejected with HTTP 422 (ResumeParseException) and no score is produced.

#### FR-3: Six-Dimension Score Calculation
The system calculates scores (0.00 to 100.00) across six discrete dimensions strictly weighted as configured in [SCORING_RULES.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/SCORING_RULES.md):
1. **ATS Compatibility (25%)**: Evaluates standard section headings, contact info presence, single-column formatting, absence of graphics, and keyword density.
2. **Skills Validation (25%)**: TF-IDF keyword extraction from the job description, exact matching, Levenshtein fuzzy distance matching (distance <= 2), and Cosine Similarity bonus calculations.
3. **Impact & Achievements (20%)**: Proportion of bullet points containing numeric metrics, percentages, and strong action verbs.
4. **Project Quality (15%)**: Assessment of project sections for titles, tech stack mentions, descriptions, and measurable outcomes.
5. **Writing Quality (10%)**: Sentence length analysis, passive voice ratio, first-person pronoun detection, filler words, and vocabulary repetition.
6. **Professionalism (5%)**: Page length estimation (> 2 pages penalized), date formatting consistency, bullet style uniformity, spelling check, and email address professionalism.

- Overall score is the exact weighted average rounded to two decimal places.
- Qualitative score tier labels are assigned strictly by the canonical thresholds (85, 70, 55, 40):
  - `>= 85.00`: **EXCELLENT** (token: `--success` `#00a46d`, label: `"Excellent Match"`)
  - `70.00 to 84.99`: **STRONG** (token: `#6fcf97`, label: `"Strong Match"`)
  - `55.00 to 69.99`: **MODERATE** (token: `--warning` `#fbc904`, label: `"Moderate Match"`)
  - `40.00 to 54.99`: **WEAK** (token: `--accent-orange` `#ff9b00`, label: `"Weak Match"`)
  - `< 40.00`: **POOR** (token: `--danger` `#ef4444`, label: `"Poor Match"`)
- Score tiers are always presented with their text tier label.

#### FR-4: Keyword Analysis
- Extracted skills from the job description are classified into:
  - **Matched Skills**: Present in resume via exact or fuzzy match (green-tinted pills).
  - **Missing Skills**: Absent from resume (orange-tinted pills).
- Skills are displayed as pill badges without individual percentage numbers.

#### FR-5: Scoring Risks Detection
- System returns deductions incurred across scoring rules with attributes: title, severity (`HIGH`, `MEDIUM`, `LOW`), description, and actionable fix.
- Severity colors:
  - **HIGH**: `#ef4444` (deduction >= 15 points)
  - **MEDIUM**: `#fbc904` (deduction 8 to 14 points)
  - **LOW**: `#999999` (deduction < 8 points)
- Clean resumes produce an empty risks list.
- Risks are presented in an expandable panel with severity badge, description, and concrete fix.

#### FR-6: Actionable Suggestions
- System generates prioritized recommendations (`HIGH`, `MEDIUM`, `LOW` priority):
  - Missing keywords.
  - Action verb replacements for weak phrasing.
  - Unquantified bullets requiring metrics or percentages.
- Displayed as a horizontal row of cards containing priority badge, title, description, and concrete example.

#### FR-7: Copyable Improvement Prompt Generation
- System generates a structured, copyable text prompt for pasting into an external AI assistant.
- The prompt includes:
  - Job title detected from the job description.
  - Missing keywords.
  - Weakest scoring sections and feedback.
  - Anti-fabrication instructions.
- Assembled entirely via server-side string concatenation with no external AI API dependency.
- Accessible via a modal dialog with focus trap, Escape key dismissal, aria-modal accessibility, and a one-click copy button with confirmation tooltip.

#### FR-8: Downloadable PDF Analysis Report
- Users can download an official PDF report via `GET /api/v1/analysis/{id}/report`.
- Server-side PDF generation using clean typography, score tier indicators, section breakdown, and risk summaries.

---

## 3. Frontend Page Specifications

Page structure, design tokens, and interactions match [FRONTEND_DESIGN.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/FRONTEND_DESIGN.md) exactly.

### 3.1 Global Navigation
- Floating pill bar centered at top of page with background `var(--bg-card)` and border `1px solid var(--border)`.
- Left: Wordmark `ResumeLens` with small SVG mark (document with magnifier).
- Center: Links for `Home` (`/`), `History` (`/history`), and `Login` (`/login`).
- Right: `Login` link when logged out, or user email with `Logout` button when logged in.
- Unauthenticated access to `History` redirects to `/login`.
- Mobile viewport: Links collapse into a menu button.

### 3.2 Landing Page (`/`)
- **Hero Section (Two Columns)**:
  - Left column:
    - Pill with spark icon and text: `"Explainable scoring"`.
    - Heading: `"See your resume the way an ATS does"`.
    - Body text: `"Upload your resume and a job description. Get a transparent score, the keywords you are missing and specific fixes in seconds."`.
    - Three badges: `"Results in seconds"`, `"File is not stored, only extracted text"`, `"Every score explained"`.
  - Right column (Upload Zone):
    - Card with dashed `var(--brand-500)` border, radius 30px.
    - `PdfIcon` (document with folded corner and sparkle).
    - Text: `"Drag and drop your resume here"`, `"or click to browse"`.
    - Button: `"Choose file"`.
    - Label: `"PDF or DOCX, up to 10 MB"`.
    - State after file chosen: File chip with remove button, optional Target role input, optional Years of experience input, required Job description textarea, and `"Analyze resume"` button.
    - State while analyzing: Displays `DocumentMockup` with continuous `ScanLine` animation and progression indicators `"Uploading"` then `"Analyzing"`.
- **Sample Report Section**:
  - Left: `DocumentMockup` component.
  - Right: Card labeled `"Sample report, not your data"` with `ScoreGauge` showing 77, label `"Strong Match"`, four checks (`"Clear section order"`, `"No critical errors"`, `"Readable by parsers"`, `"Keywords aligned"`), and keyword match bar at 85 percent (`"Matched 34 of 40 keywords"`).
- **How Your Score Is Built Section**:
  - Six chips displaying exact category names and weights from `docs/SCORING_RULES.md`:
    1. ATS Compatibility (25%)
    2. Skills Validation (25%)
    3. Impact & Achievements (20%)
    4. Project Quality (15%)
    5. Writing Quality (10%)
    6. Professionalism (5%)
- **Features Section (Three Cards)**:
  1. `"Instant analysis"`: `"Scoring runs in seconds, with a clear breakdown by category."`
  2. `"Typo tolerant keywords"`: `"Spelling and formatting variations still count as a match."`
  3. `"Specific fixes"`: `"Every risk comes with a fix, and every suggestion with an example."`
- **How It Works Section (Three Numbered Steps)**:
  1. `Upload your resume`
  2. `Add the job description`
  3. `Review your report`
- **Footer**:
  - Text: `"ResumeLens. Scores are rule based estimates and do not guarantee any ATS result."`
  - No fake user counts, testimonials, or fabricated statistics.

### 3.3 Report Page (`/report/:id`) - Redesigned Layout
0. **Header**:
   - Page heading: h1 "Your resume report".
   - Metadata chips for file name, target role, years of experience, and scan date (empty values hidden).
   - Monospace report ID with copy action.
   - Primary action "Improve my resume" (opens FixPromptModal), secondary actions "Download PDF report" and "Copy link" with helper text: "Anyone with the link can view this report."
- **Sticky In-Page Navigation**:
  - Sticky nav under the top bar with links: Overview, Action plan, Job match, Score breakdown, Things to fix, Details.
  - Active section tracked using IntersectionObserver. Collapses to scrollable chip row on mobile.
1. **Overview (`id="overview"`)**:
   - Two columns on desktop (5 and 7 cols), stacked on mobile.
   - Left card (radius 18px): ScoreGauge with overall score and tier label; segmented scale bar showing five tiers with marker at current score and ranges from TIER_RANGES; capped notice when score is capped.
   - Right card (radius 18px): Verdict headline and body from verdictFor; "Start here" top three action plan items with effort chips; "See all steps" link to action plan.
   - Four summary fact cards below: Skills covered, Things to fix, Strongest area, Needs most work.
2. **Action Plan (`id="plan"`)**:
   - Subtitle: "Do these in order. The first ones help the most."
   - Numbered cards with title, why, how, importance chip, effort chip, gain text chip.
   - "Show example" disclosure with Before and After rows when present.
   - "From your resume" quote block when present.
   - "Mark as done" checkbox in React state only (completed cards calmer with check icon, not hidden).
   - Empty state: "No action needed. Your resume matches this job well."
3. **Job Match (`id="job-match"`)**:
   - Sentence with count and percent: "You cover {matched} of {total} skills this job asks for ({percent}%)".
   - Two-part bar for matched and missing proportions.
   - Keyword band sentence ("Great coverage.", "Decent coverage. Add the missing skills you have.", "Low coverage. Many of the job's skills are missing.").
   - Two groups of pills: "Skills you already show" (check icon) and "Skills the job asks for that we did not find" (plus icon).
   - Guidance box: "How to add a skill the right way" with three rules.
4. **Score Breakdown (`id="breakdown"`)**:
   - Six category cards in order of CATEGORIES (2 cols on tablet/desktop, 1 on mobile).
   - Shows label, weight chip, score out of 100 with progress bar and tier label, points earned, what sentence, qualitative meaning line, and "How to improve" tips (or "Nothing to fix here." when score >= 85).
5. **Things to Fix (`id="issues"`)**:
   - Subtitle: "These are the same items as the action plan, with more detail."
   - Grouped by severity: Important (HIGH), Worth fixing (MEDIUM), Small polish (LOW).
   - Accordion buttons with friendly title, importance chip, effort chip; panel shows What we found (with word list appended for repeated words), Why it matters, How to fix it, Before and After examples. HIGH items start expanded.
   - Empty state: "We found no layout, writing or formatting problems."
6. **Details (`id="details"`)**:
   - Accordion 1: "How this score is calculated" (category table with weights, scores, points earned, formula line, tier ranges, and limit rule when relevant).
   - Accordion 2: "What this report cannot tell you" (DISCLAIMER plus limitations bullets).
   - Accordion 3: "Terms used" (from GLOSSARY: ATS, keyword, tier).
7. **Next Steps**:
   - Two cards: "Improve your wording" (three steps and button opening FixPromptModal) and "Updated your resume?" (Re-analyze button linking to /).
   - Footer line with fixed DISCLAIMER.

#### Features Deliberately Excluded (No Backend API Data)
- Resume document preview canvas or iframe.
- Written AI text summaries or unstructured prose.
- Letter grades (A, B, C, D, F).
- Parse rate metrics.
- Recruiter match percentages.
- Sparkline micro-charts.
- Individual per-keyword percentage match metrics.

### 3.4 History Page (`/history`)
- Available to logged-in users only.
- Historical cards displaying scan date, file name, target role, and score with tier label.
- Overall score over time line chart (hidden until 2 or more historical scan entries exist).
- Empty state with direct link to upload page when no scans are present.

---

## 4. Design System and Token Compliance

- **Surface Tokens**: `--bg-page: #0a0a0a;`, `--bg-card: #111111;`, `--bg-elevated: #181818;`, `--border: rgba(255, 255, 255, 0.08);`, `--border-strong: rgba(255, 255, 255, 0.16);`
- **Typography Colors**: `--text: #ffffff;`, `--text-secondary: #b0b0b0;`, `--text-tertiary: #999999;`, `--on-brand: #0a0a0a;`
- **Brand Ramp**: `--brand-50` through `--brand-950` (`--brand-400: #f0784d;`, `--brand-500: #e8602e;`, `--brand-600: #c14315;`)
- **Status Colors**: `--success: #00a46d;`, `--warning: #fbc904;`, `--accent-orange: #ff9b00;`, `--info: #278cff;`, `--danger: #ef4444;`. No other colors. No purple.
- **Typography Scale**: Headings in Manrope (500, 600, 700, 800), body in Inter (400, 500, 600), monospace in system monospace stack. Base 14px, small 12px, micro labels 11px uppercase with 0.08em letter spacing. Hero heading `clamp(2.5rem, 6vw, 5.75rem)` weight 800 line height 1. Section headings 22 to 28px weight 700. Gauge number 48px weight 800. Sentence case everywhere except micro labels.
- **Shape and Radii**: 4px grid. Radii: 6px small elements, 9px buttons and inputs, 12px cards, 18px large panels, 30px upload zone, 9999px fully rounded pills and badges. No other radii.
- **Glows**: Card hover `0 0 30px rgba(232, 96, 46, 0.10)`, upload zone `0 0 40px rgba(232, 96, 46, 0.15)` plus inset `0 0 20px rgba(232, 96, 46, 0.05)`, primary button hover `0 0 50px rgba(232, 96, 46, 0.20)`, floating panels `0 40px 80px rgba(0, 0, 0, 0.40)`.
- **Motion**: 150ms ease for standard transitions, 300ms for hover lifts and bar fills, 500ms for gauge ring fill, ScanLine 2.4s linear infinite. All motion disabled under `prefers-reduced-motion`.

---

## 5. Non-Functional Requirements

### 5.1 Performance and Latency
- Analysis execution under 3.0 seconds for standard two-page resumes.
- Server-side PDF report compilation under 1.5 seconds.
- Single page application initial load under 1.0 second.

### 5.2 Accessibility and States
- Every interactive element supports default, hover, focus-visible (`2px solid var(--brand-400)` with 2px offset), active, and disabled states.
- Color is never the sole indicator of meaning: all status indicators pair color with explicit text labels.
- Loading skeletons, empty states, and error alerts implemented across all routes.
- Modals implement focus trapping, `Escape` key close, and `aria-modal="true"`.
- Semantic labels present on all inputs.
- Responsive layout verified across viewports from 360px up to 1440px widths.

### 5.3 Security and Privacy
- Guest capability URLs utilize unguessable UUIDs (`/report/:id`).
- Stateless JWT authentication for user accounts.
- Password hashing with BCrypt (salt rounds >= 10).
- Zero third-party AI API exposure: resume data is analyzed locally on the server and is never sent to external AI providers.
