# FRONTEND_DESIGN.md
> Design system, visual identity, page layouts, component specifications, and interaction states for ResumeLens.
> Canonical single design source of truth for UI implementation.

---

## 1. Design Tokens

All visual presentation is driven strictly by these CSS custom properties.

### 1.1 Surface and Border Tokens
```css
:root {
  --bg-page: #0a0a0a;
  --bg-card: #111111;
  --bg-elevated: #181818;
  --border: rgba(255, 255, 255, 0.08);
  --border-strong: rgba(255, 255, 255, 0.16);
}
```

### 1.2 Typography Color Tokens
```css
:root {
  --text: #ffffff;
  --text-secondary: #b0b0b0;
  --text-tertiary: #999999;
  --on-brand: #0a0a0a;
}
```

### 1.3 Brand Palette
```css
:root {
  --brand-50: #fdf5f2;
  --brand-100: #fbebe5;
  --brand-200: #f5cfc2;
  --brand-300: #f3ab91;
  --brand-400: #f0784d;
  --brand-500: #e8602e;
  --brand-600: #c14315;
  --brand-700: #9c3611;
  --brand-800: #77290d;
  --brand-900: #562310;
  --brand-950: #38170b;
}
```

### 1.4 Status and Accent Palette
```css
:root {
  --success: #00a46d;
  --warning: #fbc904;
  --accent-orange: #ff9b00;
  --info: #278cff;
  --danger: #ef4444;
}
```
No other colors are permitted. No purple.

### 1.5 Score Tiers
Score tiers are always displayed alongside their text tier label:
- **EXCELLENT**: `#00a46d` (score >= 85)
- **STRONG**: `#6fcf97` (score 70 to 84)
- **MODERATE**: `#fbc904` (score 55 to 69)
- **WEAK**: `#ff9b00` (score 40 to 54)
- **POOR**: `#ef4444` (score < 40)

Threshold boundaries: 85, 70, 55, 40.

### 1.6 Risk Severity
- **HIGH**: `#ef4444`
- **MEDIUM**: `#fbc904`
- **LOW**: `#999999`

---

## 2. Typography

Typography is loaded from Google Fonts for headings and body copy, while monospace uses the local operating system stack.

### 2.1 Font Families
- **Headings**: Manrope (weights: 500, 600, 700, 800)
- **Body**: Inter (weights: 400, 500, 600)
- **Monospace**: System monospace stack (`ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace`)

### 2.2 Sizing, Hierarchy, and Rules
- **Base body**: 14px, regular (400) or medium (500), line height 1.5
- **Small body / metadata**: 12px, line height 1.4
- **Micro labels**: 11px, uppercase only, with letter spacing `0.08em` (strictly nothing under 11px)
- **Hero heading**: `clamp(2.5rem, 6vw, 5.75rem)`, weight 800, line height 1
- **Section headings**: 22px to 28px, weight 700, line height 1.2
- **Gauge number**: 48px, weight 800, line height 1
- **Casing rule**: Sentence case everywhere across all UI copy, buttons, labels, and headings, except for uppercase micro labels.

---

## 3. Shape, Spacing, and Effects

### 3.1 Spacing Grid
Built strictly on a **4px grid** (4px, 8px, 12px, 16px, 20px, 24px, 32px, 40px, 48px, 64px, 80px).

### 3.2 Border Radii Scale
Strictly limited to the following values (no other radii permitted):
- `6px`: Small elements (chips, tags, micro badges)
- `9px`: Buttons, text inputs, textareas, dropdown triggers
- `12px`: Cards, detail panels, dialog contents
- `18px`: Large panels, hero container surfaces
- `30px`: Resume upload zone
- `9999px` (fully rounded): Pills, pill badges, and floating pill bars

### 3.3 Card Surfaces and Borders
- Border: `1px solid var(--border)`
- Elevation: Flat by default, no heavy drop shadows

### 3.4 Glow Effects
- **Card hover**: `box-shadow: 0 0 30px rgba(232, 96, 46, 0.10);`
- **Upload zone and active elements**: `box-shadow: 0 0 40px rgba(232, 96, 46, 0.15), inset 0 0 20px rgba(232, 96, 46, 0.05);`
- **Primary button hover**: `box-shadow: 0 0 50px rgba(232, 96, 46, 0.20);`
- **Floating panels**: `box-shadow: 0 40px 80px rgba(0, 0, 0, 0.40);`

---

## 4. Graphics and Visual Assets

All graphics are original, constructed with CSS or inline SVG only. No raster images (no PNG, JPEG, WEBP) and no external image URLs.

### 4.1 Page Background
Composed of three CSS layers:
1. **Dot grid**: Radial dots of `rgba(255, 255, 255, 0.07)` spaced every 24px, masked with a radial fade gradient.
2. **Radial glow**: Warm radial glow positioned at top left: `rgba(232, 96, 46, 0.18)` fading out completely by 55%.
3. **Diagonal hairlines**: Two faint linear lines at 6% opacity crossing the canvas.

### 4.2 Custom Visual Components
- **PdfIcon**: Inline SVG depicting a document sheet with a folded upper-right corner and a small accent sparkle.
- **DocumentMockup**: Slightly rotated white card built with CSS, showing stylized grey text placeholder bars and small skill chips.
- **ScanLine**: Thin orange line (`var(--brand-500)`) with a soft glowing aura that moves vertically down the DocumentMockup continuously.
- **ScoreGauge**: Inline SVG ring with a stroke gradient transitioning from `var(--brand-400)` to `var(--brand-500)`, surrounded by a soft radial glow.
- **Icon System**: Exactly one icon set (`lucide-react`), line style. Icons are placed inside 40px rounded squares with background `rgba(232, 96, 46, 0.10)` and a 1px border `rgba(232, 96, 46, 0.20)`.

---

## 5. Motion and Transitions

- **Standard transitions**: `150ms ease` for color, background-color, border-color, opacity, and transform properties.
- **Hover lifts and progress bar fills**: `300ms ease`.
- **ScoreGauge ring fill**: `500ms ease-out` on page load.
- **ScanLine animation**: `2.4s linear infinite` translation down the mockup.
- **Accessibility requirement**: All animations and transitions are disabled completely when `@media (prefers-reduced-motion: reduce)` is active.

---

## 6. Navigation Bar

A floating pill bar centered horizontally at the top of the viewport.
- **Structure**: Rounded pill container (`border-radius: 9999px`) with background `var(--bg-card)` and border `1px solid var(--border)`.
- **Left**: Wordmark `ResumeLens` paired with a small inline SVG mark (a document sheet with a magnifying glass).
- **Center**: Navigation links: `Home` (`/`), `History` (`/history`), `Login` (`/login`).
- **Right**:
  - When logged out: `Login` link.
  - When logged in: Current user email accompanied by a `Logout` button.
- **Auth routing**: Clicking `History` when logged out redirects directly to `/login`.
- **Mobile breakpoint**: Links collapse into a menu toggle button.

---

## 7. Landing Page (`/`)

Original copy specified below must be preserved verbatim.

### 7.1 Hero Section (Two Columns)
- **Left Column**:
  - Pill badge with a spark icon and the exact text: `"Explainable scoring"`.
  - Main heading: `"See your resume the way an ATS does"`.
  - Body paragraph: `"Upload your resume and a job description. Get a transparent score, the keywords you are missing and specific fixes in seconds."`.
  - Three small badge elements:
    1. `"Results in seconds"`
    2. `"File is not stored, only extracted text"`
    3. `"Every score explained"`
- **Right Column (Upload Zone)**:
  - Card container with radius 30px and a dashed `var(--brand-500)` border.
  - Graphics: `PdfIcon`.
  - Primary text: `"Drag and drop your resume here"`.
  - Secondary text: `"or click to browse"`.
  - Button: `"Choose file"`.
  - File constraints label: `"PDF or DOCX, up to 10 MB"`.
  - **Selected file state**: The same upload card transforms to display a file chip with a file remove button, optional text input for `"Target role"`, optional numeric input for `"Years of experience"`, a required textarea for `"Job description"`, and the submission button `"Analyze resume"`.
  - **Analyzing state**: The card displays the `DocumentMockup` with the active `ScanLine` animation and progression indicators showing `"Uploading"` then `"Analyzing"`.

### 7.2 Sample Report Section
- **Heading**: `"Sample report"`
- **Left**: `DocumentMockup` component.
- **Right**: Card with label `"Sample report, not your data"`.
  - `ScoreGauge` showing numeric value `77` and tier label `"Strong Match"`.
  - Four check items:
    1. `"Clear section order"`
    2. `"No critical errors"`
    3. `"Readable by parsers"`
    4. `"Keywords aligned"`
  - Keyword match bar filled at 85 percent with text: `"Matched 34 of 40 keywords"`.

### 7.3 How Your Score Is Built Section
- **Heading**: `"How your score is built"`
- Six chips listing the exact dimension names and weights configured in `docs/SCORING_RULES.md`:
  1. `ATS Compatibility` (25%)
  2. `Skills Validation` (25%)
  3. `Impact & Achievements` (20%)
  4. `Project Quality` (15%)
  5. `Writing Quality` (10%)
  6. `Professionalism` (5%)

### 7.4 Features Section
- **Heading**: `"Features"`
- Three cards:
  1. Title: `"Instant analysis"` with description: `"Scoring runs in seconds, with a clear breakdown by category."`
  2. Title: `"Typo tolerant keywords"` with description: `"Spelling and formatting variations still count as a match."`
  3. Title: `"Specific fixes"` with description: `"Every risk comes with a fix, and every suggestion with an example."`

### 7.5 How It Works Section
- **Heading**: `"How it works"`
- Three numbered steps:
  1. `Upload your resume`
  2. `Add the job description`
  3. `Review your report`

### 7.6 Footer
- Text: `"ResumeLens. Scores are rule based estimates and do not guarantee any ATS result."`
- Strict rule: No fake user counts, testimonials, reviews, or unverified marketing statistics anywhere on the page.

---

## 8. Report Page (`/report/:id`)

Rendered in the exact sequential order listed below:

### 8.1 Top Row
- **ScoreGauge Card**: Displays the overall calculated score (0 to 100) and the corresponding score tier label.
- **File Info Card**: Displays `fileName`, scan date (`analyzedAt`), `targetRole`, and `experienceYears`. Any missing optional values are hidden cleanly.

### 8.2 Quick Insights Row
Four cards populated strictly from backend API response data:
1. Overall score tier label.
2. Keywords matched versus total extracted, accompanied by missing keywords count.
3. High and medium risk counts combined/categorized.
4. Weakest scoring section name and its numerical score.

### 8.3 Summary Card
Constructed strictly using this deterministic sentence template (no synthesized or fabricated copy):
> `"Your resume scored {score} out of 100, rated {tier}. Strongest area: {name} ({score}). Weakest area: {name} ({score}). {n} high severity risks and {m} missing keywords were found."`
- Proper pluralization applied: `"1 high severity risk"` vs `"{n} high severity risks"`, and `"1 missing keyword"` vs `"{m} missing keywords"`.
- When `{n}` equals 0, the risk phrase is replaced with: `"No high severity risks were found."`.

### 8.4 Strengths and Improvements
Derived directly from the six section scores:
- **Strengths**: Section categories with status `STRONG` or `EXCELLENT`, listed with section name and score.
- **Improvements**: Section categories with status `WEAK` or `POOR`, listed with section name and score.

### 8.5 Six Section Bars
Horizontal progress bars for all six scoring dimensions showing category title, score out of 100, weight percentage, status pill, and concise rule feedback.

### 8.6 Six Detail Cards
Six individual cards (one for each dimension) featuring:
- Standard line icon in a 40px rounded container.
- Numerical score out of 100.
- Tier label badge.
- Thin progress bar.
- One short fixed sentence explaining what the category measures:
  - **ATS Compatibility**: `"Measures standard section headings, contact information, file parsing readiness, and keyword density."`
  - **Skills Validation**: `"Measures alignment between job description keywords and candidate skills using exact and fuzzy matching."`
  - **Impact & Achievements**: `"Measures the proportion of bullet points containing measurable metrics and action verbs."`
  - **Project Quality**: `"Measures project structure including project title, technical stack mentions, descriptions, and tangible outcomes."`
  - **Writing Quality**: `"Measures conciseness, active voice usage, vocabulary repetition, and avoidance of filler phrasing."`
  - **Professionalism**: `"Measures formatting consistency, bullet uniformity, date styling, spelling accuracy, and document length."`

### 8.7 Risks and Keyword Analysis (Side-by-Side)
- **Risks Panel (Left)**: Expandable list of detected compliance deductions. Each item shows a severity badge (`HIGH`, `MEDIUM`, `LOW`), risk title, factual description, and the actionable fix recommendation.
- **Keyword Analysis Panel (Right)**: Categorized keyword pills for matched skills (green-tinted) and missing skills (orange-tinted). No individual keyword percentages are displayed.

### 8.8 Top Suggestions Row
Horizontal row of recommendation cards. Each card displays:
- Priority badge (`HIGH`, `MEDIUM`, `LOW`).
- Suggestion title.
- Clear description.
- Concrete fix example.

### 8.9 Re-analyze Card
Bottom card containing:
- Text: `"Updated your resume? Scan it again."`
- Button: `"Re-analyze"` linking back to the landing page (`/`).

### 8.10 Action Buttons
- **Download PDF report**: Initiates download from `GET /api/v1/analysis/{id}/report`.
- **Generate fix prompt**: Opens the modal dialog to view and copy the AI improvement prompt.
- **Copy link**: Copies current report URL with helper text: `"anyone with the link can view this report"`.

### 8.11 Explicitly Excluded Components
Because the backend API provides no underlying data for them, the following items are deliberately omitted:
- Resume preview iframe or canvas.
- Written AI text summaries or unstructured prose.
- Letter grades (A, B, C, D, F).
- Parse rate metrics.
- Recruiter match metrics.
- Sparkline micro-charts.
- Individual per-keyword percentage match numbers.

---

## 9. History Page (`/history`)

Accessible only to authenticated users (unauthenticated visitors are redirected to `/login`).
- **History Cards**: Displayed in descending chronological order. Each card shows scan date, resume file name, target job role, and the numerical score with tier label.
- **Score Trend Chart**: Line chart displaying overall score over time. The chart remains hidden until the user has 2 or more historical scan results.
- **Empty State**: Friendly notification informing the user that no previous scans exist, with a button to run their first analysis.

---

## 10. Interaction States and Accessibility

### 10.1 Element States
Every interactive element (buttons, links, inputs, cards) must have explicitly styled states:
- **Default**: Base token styling.
- **Hover**: Subtle brightness or background elevation shift; 300ms transition.
- **Focus-visible**: `2px solid var(--brand-400)` focus ring with `2px` offset (`outline-offset: 2px`).
- **Active**: Slight downward transform or darkened background.
- **Disabled**: Reduced opacity (0.5), cursor `not-allowed`, and pointer events disabled.

### 10.2 System States
- **Loading skeletons**: Pulse animation matching card geometries for all data fetching.
- **Empty states**: Clear, helpful guidance when lists or history cards contain no items.
- **Error states**: Form validation messages and API failure alerts styled with `var(--danger)` borders and clear recovery steps.

### 10.3 Modal Dialog Specifications
- Modal container includes focus trap trapping keyboard focus within the active dialog.
- Pressing `Escape` closes the dialog immediately.
- Container element includes `aria-modal="true"` and appropriate `role="dialog"`.

### 10.4 General Accessibility Rules
- Color is never the single carrier of information: every score tier or status includes a visible text label alongside color.
- All form controls and inputs have associated semantic `<label>` elements or accessible `aria-label` tags.
- Tested and verified across viewports from **360px** mobile widths up to **1440px** desktop widths.
