# FRONTEND_DESIGN.md
> **Design system, visual identity, page layouts, and component specifications for ResumeLens.**
> Authoritative guide for UI implementation in React + Vite.
> Linked to [API_CONTRACT.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/API_CONTRACT.md), [SCORING_RULES.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/SCORING_RULES.md), and [REQUIREMENTS.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/REQUIREMENTS.md).

---

## 1. Visual Identity & Design Principles

ResumeLens delivers a high-density, engineering-grade analytics dashboard that feels fast, analytical, and professional.

### 1.1 Core Aesthetic Guidelines
- **Theme:** Default dark theme with high contrast and dark charcoal backgrounds.
- **Primary Accent:** Burnt orange (`#D96B27` / `#E06D28`) providing high energy without harshness.
- **Card Styling:** Deep charcoal surfaces (`#161922` / `#1A1D27`), subtle borders (`#2A2E3D`), and rounded corners (`border-radius: 12px` / `16px`).
- **Dashboard Density:** Compact information density. Minimise excessive whitespace; prioritise scannability of scores, keywords, and actionable insights.
- **Iconography:** One consistent stroke-based line icon set (e.g., Lucide Icons, 1.5px–2px stroke). **Strictly no emoji, stickers, or glossy graphics** anywhere in the interface.
- **Typography:**
  - **Headings & Display:** Geometric sans-serif Google Font (e.g. `Space Grotesk`, `Outfit`, or `Plus Jakarta Sans`) with crisp letter tracking.
  - **Body & Code:** High-legibility system sans (`system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`) and monospace (`ui-monospace, "JetBrains Mono", SFMono-Regular, monospace`) for prompt blocks and metrics.

---

## 2. Color System & Semantic Tokens

### 2.1 Base Surface & Border Palette
| Token Name | Hex Code | HSL / CSS | Usage |
|:---|:---|:---|:---|
| `--bg-canvas` | `#0E1015` | `hsl(223, 20%, 7%)` | App background, full canvas |
| `--bg-surface` | `#161922` | `hsl(225, 21%, 11%)` | Primary container & card surface |
| `--bg-surface-elevated` | `#1E222E` | `hsl(224, 21%, 15%)` | Hover states, modals, elevated panels |
| `--border-subtle` | `#262B3B` | `hsl(225, 22%, 19%)` | Inactive card borders, dividers |
| `--border-active` | `#3D445D` | `hsl(226, 21%, 30%)` | Focused borders, active state highlights |
| `--text-primary` | `#F1F3F9` | `hsl(225, 30%, 96%)` | Main headings, primary content text |
| `--text-secondary` | `#9DA4BD` | `hsl(227, 18%, 68%)` | Labels, subtitles, metadata |
| `--text-muted` | `#606882` | `hsl(226, 15%, 44%)` | Placeholders, disabled states |

### 2.2 Accent & Brand Palette
| Token Name | Hex Code | Usage |
|:---|:---|:---|
| `--primary-orange` | `#E06D28` | Primary brand accent, primary CTA buttons, active tabs |
| `--primary-orange-hover` | `#C85B1B` | Button hover and pressed states |
| `--primary-orange-subtle` | `rgba(224, 109, 40, 0.12)` | Active pill backgrounds, badge backgrounds |

### 2.3 Semantic Score Tiers & Status Colors
In strict accordance with [SCORING_RULES.md](file:///c:/Users/LENOVO/Desktop/Resumelens/docs/SCORING_RULES.md), the system defines four discrete status thresholds: **85, 70, 55, and 40**. Colors are applied strictly by these bounds:

| Score Range | Status Label | Status Color Token | Hex | UI Semantic |
|:---|:---|:---|:---|:---|
| **≥ 85.00** | **Excellent Match** | `--color-score-excellent` | `#38A169` (Muted Green) | Exceeds ATS standards; high compatibility |
| **70.00 – 84.99** | **Strong Match** | `--color-score-strong` | `#319795` (Teal) | Strong candidate; minor adjustments |
| **55.00 – 69.99** | **Moderate Match** | `--color-score-moderate` | `#D69E2E` (Amber) | Average alignment; notable gaps |
| **40.00 – 54.99** | **Weak Match** | `--color-score-weak` | `#DD6B20` (Burnt Orange) | High risk of ATS rejection |
| **< 40.00** | **Poor Match** | `--color-score-poor` | `#E53E3E` (Muted Crimson Red) | High severity; fails basic ATS checks |

> [!IMPORTANT]
> **Red usage constraint:** Crimson Red (`#E53E3E`) is reserved strictly for scores `< 40.00` and `HIGH` severity risk alerts. It is never used for cosmetic accents or standard warnings.

---

## 3. Typography Scale

```css
@import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@500;600;700&family=JetBrains+Mono:wght@400;500&display=swap');

:root {
  --font-heading: 'Space Grotesk', -apple-system, sans-serif;
  --font-body: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  --font-mono: 'JetBrains Mono', ui-monospace, monospace;
}
```

| Element | Font Family | Size | Weight | Line Height | Letter Spacing |
|:---|:---|:---|:---|:---|:---|
| **H1 (Page Title)** | `var(--font-heading)` | `28px` (`1.75rem`) | 700 | 1.2 | `-0.02em` |
| **H2 (Section Header)** | `var(--font-heading)` | `20px` (`1.25rem`) | 600 | 1.3 | `-0.01em` |
| **H3 (Card Header)** | `var(--font-heading)` | `16px` (`1.00rem`) | 600 | 1.4 | `0` |
| **Body (Default)** | `var(--font-body)` | `14px` (`0.875rem`) | 400 | 1.5 | `0` |
| **Caption / Meta** | `var(--font-body)` | `12px` (`0.75rem`) | 500 | 1.4 | `+0.01em` |
| **Score Metric (Large)** | `var(--font-heading)` | `44px` (`2.75rem`) | 700 | 1.0 | `-0.03em` |
| **Code / LaTeX Prompt** | `var(--font-mono)` | `12.5px` (`0.78rem`) | 400 | 1.6 | `0` |

---

## 4. Page Architecture & User Flows

The application consists of four responsive pages:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        RESUMELENS NAVIGATION                           │
│  [Logo: ResumeLens]         [Upload]    [History*]    [Login / User]   │
└────────────────────────────────────────────────────────────────────────┘
```
*\*History visible/active only when authenticated.*

### 4.1 Page 1: Login / Signup (`/auth`)
- **Purpose:** Optional account creation for users who want persistent history across devices.
- **Key Layout:** Centered card (`420px` max-width) on canvas background.
- **Elements:**
  - Tab switcher: `Log In` vs `Create Account`.
  - Input fields: `Email`, `Password`, `Full Name` (on signup tab only).
  - Primary Submit Button: `Sign In` / `Create Account` (Burnt Orange).
  - **Prominent Guest Exit:** Highly visible divider with `"Don't want an account?"` and a secondary button: `Continue as Guest →`. Clicking takes the user directly to the Upload page without storing credentials.

### 4.2 Page 2: Upload Page (`/`)
- **Purpose:** Intake resume document and target job description in a two-column compact layout.
- **Left Column: Resume Intake (`45%` width):**
  - Drag-and-drop file dropzone accepting `.pdf` and `.docx` (10MB limit).
  - File preview pill displaying file name, size, parsed status, and a `Remove` button.
  - Optional metadata inputs: `Target Role` and `Years of Experience`.
- **Right Column: Job Description Intake (`55%` width):**
  - Multi-line textarea for pasting job description text.
  - Character and word counter indicator.
- **Bottom Action Bar:**
  - Full-width CTA bar with primary button: `Analyze Resume & Predict Score →`.
  - Loading spinner with progress states (`"Extracting text..."`, `"Calculating TF-IDF...", "Evaluating ATS rules..."`).

### 4.3 Page 3: Report & Results Dashboard (`/report/:matchResultId`)
- **Purpose:** Full analytics breakdown, scores, risks, suggestions, prompt copy, and PDF download.
- **Header Summary Strip:**
  - Target Job Title, Resume File Name, Analysis Timestamp.
  - Action Buttons: `Download PDF Report` (line icon + button), `Get AI Fix Prompt` (burnt orange primary button).
- **Row 1: Score Overview:**
  - Left: `ScoreGauge` (overall score circular dial with animated sweep, status badge, e.g., "Strong Match").
  - Center/Right: 3 × `StatCard` displaying Keyword Match %, Impact Bullet %, and High Severity Risk Count.
- **Row 2: 6-Dimension Score Breakdown:**
  - 6 × `SectionScoreBar` displaying dimension score, weight, status pill, and concise feedback.
- **Row 3: Keyword Analysis & Missing Skills:**
  - Two adjacent sub-panels:
    - `Matched Keywords`: Green-tinted `KeywordPill` list.
    - `Missing Keywords`: Amber/Red-tinted `KeywordPill` list.
- **Row 4: Actionable Recommendations & Risks:**
  - Left: List of `RiskCard` items (formatting risks, multi-column layout, contact info issues).
  - Right: List of `SuggestionCard` items (categorised into `HIGH`, `MEDIUM`, `LOW` priority).

### 4.4 Page 4: History Dashboard (`/history`)
- **Purpose:** Chronological record of previous analyses (authenticated users only; guest redirected to `/auth`).
- **Summary Metrics Strip:** Total Resumes Analyzed, Average Overall Score, Highest Achieved Score.
- **Score Trend Chart:** Compact line chart showing overall score progression over time.
- **History List:** Grid or list of `HistoryCard` items with date, job title, score badge, and quick `View Report →` link.

---

## 5. UI Component Specifications

### 5.1 `ScoreGauge`
- **Description:** Circular SVG gauge showing 0–100 overall score.
- **Props:** `score` (Number), `statusLabel` (String), `size` (Number, default 160px).
- **Visuals:** Background track in `--border-subtle`, active sweep stroke colored by semantic tier (`--color-score-*`). Center displays large numeric score (`44px Space Grotesk`) and status pill directly below.

### 5.2 `SectionScoreBar`
- **Description:** Compact horizontal progress bar for each of the 6 dimensions.
- **Props:** `sectionName` (String), `score` (Number), `weight` (String, e.g. "25%"), `status` (String), `feedback` (String).
- **Visuals:** Top row shows dimension label and numeric score (`82/100`). Track height 6px, rounded fill with color mapped from score threshold. Collapsible one-line feedback message underneath.

### 5.3 `KeywordPill`
- **Description:** Tag badge for skills and keywords.
- **Props:** `keyword` (String), `type` (`"matched"` | `"missing"`), `onRemove` (optional).
- **Visuals:**
  - Matched: Background `rgba(56, 161, 105, 0.12)`, border `#2E8540`, text `#68D391`.
  - Missing: Background `rgba(224, 109, 40, 0.12)`, border `#B85D19`, text `#FBD38D`.

### 5.4 `RiskCard`
- **Description:** Alert card for ATS compliance infractions.
- **Props:** `title` (String), `deduction` (Number, e.g., -10), `severity` (`"high"` | `"medium"` | `"low"`), `details` (String).
- **Visuals:** Left border indicator: Red for high, Amber for medium. Line icon (e.g. `AlertTriangle`). Displays deduction badge (e.g., `-10 pts`).

### 5.5 `SuggestionCard`
- **Description:** Concrete recommendation card for improving resume quality.
- **Props:** `priority` (`"HIGH"` | `"MEDIUM"` | `"LOW"`), `title` (String), `description` (String), `example` (String).
- **Visuals:** Card with priority badge, description, and an inline example block highlighted with a subtle monospace box.

### 5.6 `StatCard`
- **Description:** Small metric indicator card for quick stats.
- **Props:** `icon` (Component), `label` (String), `value` (String/Number), `subtext` (String).
- **Visuals:** Charcoal background, 1px border, compact 80px height.

### 5.7 `HistoryCard`
- **Description:** Row or card in user history list.
- **Props:** `item` (`HistoryItemResponse`), `onClick` (Function).
- **Visuals:** Job title, resume file name, formatted date, score badge, and chevron arrow.

### 5.8 `FixPromptModal`
- **Description:** Modal dialog for previewing, customizing, and copying the LaTeX AI improvement prompt.
- **Props:** `isOpen` (Boolean), `matchResultId` (Long), `onClose` (Function).
- **Features:**
  - Dropdown selector for `Template Style` (`"Simple Single-Column"`, `"Modern Clean"`, `"Compact Academic"`).
  - Copy to Clipboard button with visual confirmation tooltip (`"Copied!"`).
  - Scrollable pre-formatted code container with syntax highlight / monospace display.
  - Plain English disclaimer: *"ResumeLens does not call AI APIs. Paste this prompt into ChatGPT, Claude, or Gemini to receive your updated LaTeX resume."*
