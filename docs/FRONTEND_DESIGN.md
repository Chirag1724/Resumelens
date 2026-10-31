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

The report page presents a clear overview for non-technical candidates alongside deep inspectability for technical readers.

### 8.1 Layout and Spacing
- Content container: maximum width 1360px (`max-w-7xl xl:max-w-[1360px]`), centered, with horizontal padding of 16px on mobile, 24px on tablet, and 32px on desktop.
- Spacing scale: strictly based on an 8px grid scale. Vertical spacing between page sections is 64px on desktop, 48px on tablet, and 40px on mobile.
- Cards: rendered using the card surface token (`var(--bg-card)`), 1px border (`var(--border)`), radius 12px (18px for the two large hero cards in Overview), padding 24px (20px on mobile), 16px between elements inside a card, and 24px grid gaps (16px on mobile).
- Section header pattern: small label in sentence case (13px, brand color), an h2 heading (24px to 28px, Manrope 700), a one line subtitle (15px, secondary text, maximum line length 60ch), followed by 20px vertical spacing before section content.
- Body text: 15px with line height 1.6 and a maximum line length of 68ch. Monospace font is reserved strictly for report UUID identifiers.
- Heading hierarchy: single h1 on the page ("Your resume report"), h2 for each section, h3 inside cards, with `aria-labelledby` on every section pointing to its h2 heading.
- Sticky navigation: in-page navigation bar sticky beneath the top bar with links for Overview, Action plan, Job match, Score breakdown, Things to fix, and Details. Active link is tracked and highlighted using `IntersectionObserver` with zero polling. Collapses into a horizontally scrollable chip row on mobile screens. Every section defines `scroll-margin-top` so headings remain visible when navigated to.
- Top navigation bar: solid surface with at least 92 percent opacity and backdrop blur to prevent page content from bleeding through during scroll.

### 8.2 Sequential Section Structure

#### Section 0: Header
- Heading: h1 with text "Your resume report".
- Metadata chips: displays file name, target role, years of experience, and scan date. Empty or null fields are hidden cleanly without empty placeholders.
- Identifier: monospace display of the report UUID with an inline copy action.
- Actions: primary button "Improve my resume" (opens `FixPromptModal`), secondary button "Download PDF report", and secondary button "Copy link" with copy confirmation. Helper text beneath buttons: "Anyone with the link can view this report."

#### Section 1: Overview (`id="overview"`)
- Hero row: two-column layout on desktop (5 and 7 columns out of 12), stacked on mobile.
- Left hero card (radius 18px): `ScoreGauge` showing overall score out of 100 and qualitative tier label with icon. Segmented scale bar showing all five tiers (Poor, Weak, Moderate, Strong, Excellent) with tier ranges from `TIER_RANGES` and a position marker at the candidate score. When `scoreCapped` is true, displays a highlighted notice derived from `capInfo`: "Your score is held at {cap}. Your resume on its own would score {uncapped}." and "Raise your skills match from {skillsNow} to {threshold} to lift this limit. That is about {moreSkills} more of the job's skills." (omitted if `moreSkills` is null).
- Right hero card (radius 18px): candidate verdict headline and body copy from `verdictFor(overallScore)`. "Start here" section featuring the first three action plan items as a numbered list with item titles and effort chips, accompanied by a "See all steps" link pointing to `#plan`.
- Summary fact cards: row of four metric cards beneath the hero cards derived from `summaryFacts` (Skills covered, Things to fix, Strongest area, Needs most work), each featuring an icon, primary value or name, and a short explanatory sentence.

#### Section 2: Action Plan (`id="plan"`)
- Subtitle: "Do these in order. The first ones help the most."
- Numbered cards for each recommended action item derived from `buildActionPlan` showing title, why it matters, how to do it, importance chip, effort chip, and score gain estimate chip when applicable.
- Disclosures: "Show example" toggle revealing Before and After comparative rows when concrete examples exist. "From your resume" quotation block when contextual excerpts are available.
- State: "Mark as done" interactive checkbox stored in React component state only (no browser storage). Completed items remain visible in a calmer, dimmed presentation with a green completion check.
- Empty state: "No action needed. Your resume matches this job well." when no action items exist.

#### Section 3: Job Match (`id="job-match"`)
- Metric summary: sentence stating "You cover {matched} of {total} skills this job asks for ({percent}%)".
- Visual bar: two-part segmented bar displaying matched and missing proportions with accessible progressbar semantics.
- Coverage band: descriptive sentence based on `keywordBand` ("Great coverage.", "Decent coverage. Add the missing skills you have.", or "Low coverage. Many of the job's skills are missing.").
- Skill pill groups: "Skills you already show" with check icons and "Skills the job asks for that we did not find" with plus icons. Empty groups are hidden; a positive confirmation message is displayed when zero skills are missing.
- Best practice guidance: callout titled "How to add a skill the right way" with three rules: add only genuine skills, use exact job wording, and integrate skills into project or experience bullets rather than isolated lists.

#### Section 4: Score Breakdown (`id="breakdown"`)
- Six category cards in canonical order: ATS compatibility, Skills match, Impact and results, Project quality, Writing quality, Professionalism. Arranged in two columns on tablet and desktop, single column on mobile.
- Card contents: category label, weight percentage chip, score out of 100 with progress bar and tier label, points earned ("Points earned: X of {weight}"), category definition sentence, qualitative meaning line (good, partial, or needs), and a "How to improve" list of tips when score is below 85, or "Nothing to fix here." when 85 or above.

#### Section 5: Things to Fix (`id="issues"`)
- Subtitle: "These are the same items as the action plan, with more detail."
- Grouped by severity: Important (HIGH), Worth fixing (MEDIUM), and Small polish (LOW). Empty severity tiers are hidden.
- Accordion items: real button triggers with `aria-expanded` showing friendly title, importance chip, and effort chip. High importance items start expanded by default.
- Panel detail: What we found (with raw repeated word listings appended for repeated words risks), Why it matters, How to fix it, and Before/After examples where available.
- Empty state: "We found no layout, writing or formatting problems." when zero risks are detected.

#### Section 6: Details (`id="details"`)
- Accordion 1 ("How this score is calculated"): breakdown table showing all six dimensions with weights, scores, and points earned, the formula line "Overall score = sum of points earned", tier threshold ranges from `TIER_RANGES`, and when relevant the skill threshold capping rule in plain words ("If the skills match is below 40, the overall score is limited to 54. From 40 to 59 it is limited to 69.").
- Accordion 2 ("What this report cannot tell you"): legal disclaimer text alongside explicit limitations noting keyword/wording comparison, English language optimization, and absence of factual verification.
- Accordion 3 ("Terms used"): concise explanations of core terminology from `GLOSSARY` (ATS, keyword, tier).

#### Section 7: Next Steps
- Two-card layout:
  1. "Improve your wording": three numbered steps ("Generate the improvement prompt", "Paste it into an AI assistant", "Open the result in a LaTeX editor such as Overleaf") and an action button opening `FixPromptModal`.
  2. "Updated your resume?": description and Re-analyze button navigating back to the scanner.
- Page footer: fixed disclaimer statement from `DISCLAIMER`.

### 8.3 State and Accessibility Rules
- Interactive states: hover, focus-visible (2px ring), active, and disabled states implemented across all buttons, inputs, links, and accordions.
- Multi-channel signaling: color is never the sole indicator; every score, tier, and severity is paired with an explicit text label and an icon.
- Contrast compliance: text on every surface maintains at least a 4.5 to 1 contrast ratio.
- Responsive flow: zero horizontal scrolling at 360px viewport width.
- Motion: respects `prefers-reduced-motion` settings.
- Null-safety: optional backend fields (`targetRole`, `experienceYears`, `risks`, `suggestions`, keyword arrays) gracefully handle null or empty states without layout shift or script exceptions.
- Exclusion: `improvementPrompt` from the API response is never rendered directly in the report view. Data is never fabricated.

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

---

## 11. Content Layer and Report Copy

The content layer provides clear text and guidance for your report.
All copy uses plain English in the second person.
Sentences stay under 20 words.
Instructions start with a verb.
Copy avoids scary words and promises.
The file `src/content/reportCopy.js` exports verdicts, categories, risk guidance, effort labels, and a glossary.
The model layer in `src/lib/reportModel.js` formats your action plan and score insights.
