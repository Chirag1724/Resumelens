# SCORING_RULES.md
> **Canonical definition of every scoring formula used by ResumeLens.**
> Any code that touches scoring MUST match this file exactly. If a discrepancy is found between code and this file, the code is wrong — update the code, not this file (unless a deliberate formula change is being logged in DECISIONS_LOG.md).

---

## Score Architecture Overview

The overall score is calculated from a weighted average of six dimension scores, each on a 0 to 100 scale.

| Dimension | Weight |
|-----------|--------|
| ATS Compatibility | 25% |
| Skills Validation | 25% |
| Impact & Achievements | 20% |
| Project Quality | 15% |
| Writing Quality | 10% |
| Professionalism | 5% |

**Uncapped Score Formula:**
```
uncappedScore =
  (atsScore       * 0.25) +
  (skillsScore    * 0.25) +
  (impactScore    * 0.20) +
  (projectScore   * 0.15) +
  (writingScore   * 0.10) +
  (proScore       * 0.05)
```

All intermediate and final scores are rounded to **two decimal places**.

### Job Fit Score Cap

After computing the weighted sum, cap the overall score using the Skills Validation score:
- **Below 40**: caps the overall score at **54.0**.
- **From 40 up to but not including 60**: caps the overall score at **69.0**.
- **60 or above**: applies no cap (`overallScore = uncappedScore`).

The overall status label derives from the capped score.
The API response returns:
- `uncappedScore`: the plain weighted sum, rounded to two decimal places.
- `scoreCapped`: boolean, true only when the cap lowered the score (`overallScore < uncappedScore`).

For GET requests, `uncappedScore` is computed from the stored six section scores with the same weights, so no new database column is needed, and POST and GET return identical values. History continues to use the stored overall score.

---

## 1. ATS Compatibility Score (weight: 25%)

**Base score:** 100

Apply the following **deductions** (non-cumulative caps noted where applicable):

| Rule | Deduction | Notes |
|------|-----------|-------|
| Standard section headings missing (e.g., "Education", "Experience", "Skills") | -8 per missing heading | Max deduction from this rule: -24. Evaluated against canonical section names detected via the section alias table. |
| Contact info block absent (email or phone not found) | -10 | |
| Resume uses tables or multi-column layout (detected via parser heuristic) | -10 | |
| Resume uses graphics, images, or non-standard fonts (detected via heuristic) | -8 | Graphics by displayed size: Replace the pixel size rule completely. For PDF, process each page content stream with a PDFStreamEngine subclass that handles the Do operator, including images nested in form XObjects, and read the drawn width and height in points from the current transformation matrix. Count an image only when both drawn width and drawn height are at least 72 points. Vector drawings and form XObjects without an image are never counted. For DOCX use the drawn extent of each inline or anchored picture (914400 EMU per inch) with the same 72 point rule. |
| Keyword density below 30% of JD keywords present in resume | -15 | Threshold: fewer than 30% of extracted JD keywords found (canonical match or strict fuzzy match) |
| Keyword density 30% to 50% | -8 | Partial penalty; only one of the two keyword density rules applies |
| No measurable achievements found | -5 | Fires only when no bullet or sentence in the whole resume text contains a standalone number (number followed by %, +, x, k, M or B, or any number of 2 or more digits not glued to letters) |
| File with no extractable text (for example a scanned image PDF) | Rejected at upload with HTTP 422 and a clear message; no score is produced | |

**Floor:** ATS score cannot go below 0.

### Section Heading Normalization & Canonical Alias Table
Standard section headings are extracted by `ResumeParserService` using a constant alias table mapping canonical section names to alias sets across the five required sections:
- **EXPERIENCE**: experience, work experience, professional experience, internships, employment history
- **EDUCATION**: education, academic background, qualifications
- **SKILLS**: skills, technical skills, key skills, core competencies
- **PROJECTS**: projects, academic projects, personal projects
- **CERTIFICATIONS**: certifications, courses, achievements, awards

**Heading Classification Criteria:**
A line is treated as a section heading only when:
1. It is at most 40 characters in length.
2. It contains at most 5 words.
3. It does not end with a period.
4. Its normalized form (lowercase with `Locale.ROOT`, letters only, single spaces) matches an alias in the table exactly.

Normal prose sentences are never treated as section headings.
The three required sections evaluated for the ATS Compatibility missing headings deduction remain Education, Experience, and Skills (-8 per missing heading, max deduction -24). Project Quality evaluates the canonical Projects section.

---

## 2. Skills Validation Score (weight: 25%)

### Extraction (up to 30 keywords)
1. **Skill Dictionary First**: Technical keywords are matched against a curated dictionary of technical terms (loaded once at startup into an immutable set from `skills.txt`). Multiword dictionary terms (e.g., "deep learning", "computer vision", "github actions", "data science", "apache spark", "apache kafka") are matched as whole phrases and preserved as ONE keyword without splitting.
2. **TF-IDF Fallback**: Remaining keyword slots up to 30 are filled with the highest TF-IDF terms using the sentence-level mini-corpus.
3. **Generic Words Filter**: Terms in the generic words set (over 190 words, including engineer, engineers, practices, practice, evaluate, techniques, technique, degree, bachelor, master, phd, field, science, apache, and the generic phrase "computer science") and tokens shorter than 3 characters are excluded from keyword extraction and never become keywords. Multiword skills "data science", "apache spark", and "apache kafka" are in skills.txt so they stay one keyword.

### Matching Rules
The substring contains rule and the global edit distance 2 rule are removed for all keywords.
- **Canonical Match**: A JD keyword is matched when its canonical form equals the canonical form of a resume token, or of two adjacent resume tokens joined together.
- **Canonical Form**:
  - Convert to lowercase with `Locale.ROOT`.
  - Special cases handled before stripping: `c++` -> `cpp`, `c#` -> `csharp`, `.net` -> `dotnet`.
  - Remove all characters that are not ASCII letters or digits.
  - Strip a trailing "js" when the remaining length is strictly greater than 3 (e.g., reactjs, react.js, react js, and react are equal; node js, nodejs, and node.js are equal; mongo db and mongodb are equal).
- **Skill Aliases**: One constant alias table with exact canonical equality only (no fuzzy matching), used across every matching code path:
  - JD keyword `sql` is matched by resume tokens `mysql`, `postgresql`, `postgres`, `sqlite`, `mariadb`, `mssql`, `plsql`, or `tsql`.
  - `postgresql` and `postgres` match each other.
  - `javascript` and `js` match each other.
  - `typescript` and `ts` match each other.
  - `kubernetes` and `k8s` match each other.
  - `machine learning` and `ml` match each other.
- **Strict Fuzzy Matching**: Allowed only when the canonical keyword has at least 6 characters and the first two characters match:
  - Allowed edit distance is 1 for canonical length 6 to 8 characters.
  - Allowed edit distance is 2 for canonical length 9 or more characters.
  - Keywords with fewer than 6 characters are never fuzzy matched.

### Scoring Formula
```
matchedSkills   = count of JD keywords found in resume (canonical or strict fuzzy)
totalJdSkills   = total extracted JD keywords (capped at 30)

skillsScore = (matchedSkills / totalJdSkills) * 100
```

Additionally, a **Cosine Similarity bonus** (up to +10 points, capped so total never exceeds 100) is applied:

```
cosineBonus = cosineSimilarity(resume_tfidf_vector, jd_tfidf_vector) * 10
skillsScore = min(100, skillsScore + cosineBonus)
```

### Single Source of Truth
`SuggestionEngine` and the Skills Validation section use the identical extraction and matching rules, guaranteeing that missing keywords reported in suggestions equal the Skills Validation missing keywords list.

---

## 3. Impact & Achievements Score (weight: 20%)

Count bullet points that contain at least one:
- Numeric value (e.g., "increased by 40%", "managed 12 engineers")
- Strong action verb from the predefined verb list in ATSScoreEngine

Bullet markers: symbols `•`, `●`, `▪`, `◦`, and `*` always count as bullet markers. Symbols `-` and `–` count as markers only when at least 2 lines in the document start with that symbol followed by a space (so a single wrapped line starting with a dash does not count).

```
impactPercentage = (impactBullets / totalBullets) × 100

if totalBullets == 0 → impactScore = 0
```

**Percentage-to-score mapping:**

| impactPercentage | impactScore |
|-----------------|-------------|
| ≥ 70% | 100 |
| 50%–69% | 80 |
| 30%–49% | 60 |
| 10%–29% | 40 |
| 1%–9% | 20 |
| 0% | 0 |

---

## 4. Project Quality Score (weight: 15%)

Evaluated only if a "Projects" section is detected.  
Each project entry is assessed for **four sub-criteria** (each 0 or 1):

| Sub-criterion | Rule |
|---------------|------|
| Has a title | Project heading/name detected |
| Has a tech stack mention | At least one known technology keyword found |
| Has a description | At least 10 words in the project body |
| Has a measurable outcome | Contains a number or a result-oriented keyword (e.g., "deployed", "reduced", "achieved") |

```
projectSubScore = (sum of sub-criteria met / 4) for each project
projectScore    = average of all projectSubScores × 100

If no Projects section detected → projectScore = 50  (neutral, not penalised)
```

---

## 5. Writing Quality Score (weight: 10%)

**Base score:** 100

| Deduction Rule | Amount |
|---------------|--------|
| Average sentence length > 35 words | –10 |
| Use of first-person pronouns (I, me, my, we, our) | –15 |
| Repetition: same word appears > 5 times (excluding skill dictionary terms, generic words set, words < 4 chars, and stopwords) | -5 per repeated word, max -15 | When the Repeated Words risk fires, its description ends with: "Words used more than 5 times: managed (8), custom (6)." (up to 5 words ordered by count descending then alphabetically). Deduction and threshold do not change. |
| Passive voice ratio > 40% of sentences | -10 |

**Floor:** Writing score cannot go below 0.

---

## 6. Professionalism Score (weight: 5%)

**Base score:** 100

| Deduction Rule | Amount |
|---------------|--------|
| Spelling errors detected (simple dictionary check against a curated word list) | -5 per error, max -20 |
| Inconsistent date formatting (mix of "Jan 2023" and "01/2023" styles) | -10 |
| Inconsistent bullet style (multiple marker styles detected among active bullet markers) | -5 |
| Email address not professional (contains slang, numbers in a way suggestive of casual email) | -10 |
| Resume length > 2 pages (estimated at > 700 words for a single-column resume) | -5 |

**Floor:** Professionalism score cannot go below 0.

---

## Status Label Thresholds

A single shared method (`SectionScore.deriveStatus`) maps numerical scores across the canonical 85, 70, 55, 40 boundary thresholds:

| Score Range | Section Status (`SectionScore.status`) | Overall Status Label (`statusLabel`) |
|-------------|----------------------------------------|--------------------------------------|
| >= 85 | **EXCELLENT** | **Excellent Match** |
| 70 - 84 | **STRONG** | **Strong Match** |
| 55 - 69 | **MODERATE** | **Moderate Match** |
| 40 - 54 | **WEAK** | **Weak Match** |
| < 40 | **POOR** | **Poor Match** |

---

## Improvement Prompt Generation

After scoring, the system generates a structured copyable prompt. The prompt must include:
1. The job title (if detectable from the JD).
2. The full list of missing JD keywords not found in the resume.
3. The sections with the lowest scores and their feedback strings.
4. A fixed instruction block asking the external AI to rewrite or improve the corresponding resume sections.

The system **does not call any external API** to generate this prompt. It is built by string assembly in `PromptBuilderService`.

---

## Scoring Risks System

When deductions fire during evaluation in `ATSScoreEngine`, corresponding risk objects are recorded and attached to the score result and REST responses.

### 1. Risk Attributes
Each risk consists of:
- `title`: Standard rule title.
- `severity`: `"HIGH"`, `"MEDIUM"`, or `"LOW"`.
- `description`: Plain, factual description of the issue.
- `fix`: Actionable advice on resolving the deduction.

### 2. Severity Mapping
Severity is derived directly from the deduction amount incurred by the firing rule:
- **HIGH**: deduction of 15 points or more (e.g., keyword density < 30%, first-person language, missing sections >= 2).
- **MEDIUM**: deduction of 8 to 14 points (e.g., missing contact info, multi-column/table layout, graphics present, keyword density 30% to 50%, long sentences, passive voice > 40%, inconsistent dates, casual email).
- **LOW**: deduction below 8 points (e.g., no measurable achievements, inconsistent bullets, over 700 words).

### 3. Fired Rules & Definitions
All risk titles, descriptions, and fixes are maintained in a constant map (`RISK_DEFINITIONS`) in `ATSScoreEngine`:
1. **Missing Section Headings**: missing standard headings (Education, Experience, Skills) (-8 to -24).
2. **No Contact Info**: missing email or phone (-10).
3. **Multi-Column or Table Layout**: multi-column or table formatting detected (-10).
4. **Graphics Present**: embedded images or graphics detected (-8).
5. **Keyword Density Below 30 Percent**: JD keyword match < 30% (-15).
6. **Keyword Density Between 30 and 50 Percent**: JD keyword match 30% to 50% (-8).
7. **No Measurable Achievements**: no numbers or metric indicators detected (-5).
8. **First-Person Language**: usage of I, me, my, we, our (-15).
9. **Filler Words**: weak phrases like "responsible for", "helped with" (-3 per, max -15).
10. **Repeated Words**: words appearing > 5 times (-5 per, max -15). The risk description ends with: "Words used more than 5 times: word1 (count1), word2 (count2)." for up to 5 words ordered by count descending then alphabetically. GET recomputes the identical text.
11. **Long Sentences**: average sentence length > 35 words (-10).
12. **Passive Voice Over 40 Percent**: passive sentence ratio > 40% (-10).
13. **Spelling Errors**: common misspelled words found (-5 per, max -20).
14. **Inconsistent Dates**: mix of alpha and numeric date styles (-10).
15. **Inconsistent Bullets**: mix of bullet characters (-5).
16. **Casual Email**: informal email pattern (-10).
17. **Over 700 Words**: resume text exceeding 700 words (-5).

### 4. Ordering & Determinism
- Risks are sorted in order of severity: `HIGH` first, then `MEDIUM`, then `LOW`.
- Clean resumes with no triggered deductions produce an empty risks list (`[]`).
- `GET /api/v1/analysis/{id}` recomputes risks deterministically from persisted inputs, guaranteeing identical output to `POST /api/v1/analysis/match`.
- The `hasFormattingRisk` scoring input is persisted as a boolean column on `Resume` alongside `hasGraphicsRisk`.

---

## Actionable Suggestions Condition

Actionable recommendations are generated in `SuggestionEngine`:
- **"Review overall resume structure"**: Appears only when the ATS Compatibility section score is strictly below 70 (`atsScore < 70`), not when the overall score is low (e.g. overall score capped to 54 with ATS Compatibility 85 produces no structure suggestion; ATS Compatibility 60 produces it).
- **"Add Missing Technical Keywords"**: Produced when missing keywords from Skills Validation are present.
- **"Replace weak action verbs"**: Produced when weak verbs are detected in the resume text.
- **"Quantify your achievements"**: Produced when bullets lack numbers or measurable outcomes.
- GET and POST return identical suggestions for the same result.

