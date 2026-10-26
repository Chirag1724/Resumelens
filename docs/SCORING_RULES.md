# SCORING_RULES.md
> **Canonical definition of every scoring formula used by ResumeLens.**
> Any code that touches scoring MUST match this file exactly. If a discrepancy is found between code and this file, the code is wrong — update the code, not this file (unless a deliberate formula change is being logged in DECISIONS_LOG.md).

---

## Score Architecture Overview

The overall score is a **weighted average of six dimension scores**, each on a 0–100 scale.

| Dimension | Weight |
|-----------|--------|
| ATS Compatibility | 25% |
| Skills Validation | 25% |
| Impact & Achievements | 20% |
| Project Quality | 15% |
| Writing Quality | 10% |
| Professionalism | 5% |

**Overall Score Formula:**
```
overallScore =
  (atsScore       × 0.25) +
  (skillsScore    × 0.25) +
  (impactScore    × 0.20) +
  (projectScore   × 0.15) +
  (writingScore   × 0.10) +
  (proScore       × 0.05)
```

All intermediate and final scores are rounded to **two decimal places**.

---

## 1. ATS Compatibility Score (weight: 25%)

**Base score:** 100

Apply the following **deductions** (non-cumulative caps noted where applicable):

| Rule | Deduction | Notes |
|------|-----------|-------|
| Standard section headings missing (e.g., "Education", "Experience", "Skills") | –8 per missing heading | Max deduction from this rule: –24 |
| Contact info block absent (email or phone not found) | –10 | |
| Resume uses tables or multi-column layout (detected via parser heuristic) | –10 | |
| Resume uses graphics, images, or non-standard fonts (detected via heuristic) | –8 | |
| Keyword density below 30% of JD keywords present in resume | –15 | Threshold: fewer than 30% of extracted JD keywords found (exact or fuzzy via Levenshtein ≤ 2 edits) |
| Keyword density 30%–50% | –8 | Partial penalty; only one of the two keyword density rules applies |
| No measurable achievements found (checks the whole resume text for any digit or percentage) | -5 | |
| File with no extractable text (for example a scanned image PDF) | Rejected at upload with HTTP 422 and a clear message; no score is produced | |

**Floor:** ATS score cannot go below 0.

---

## 2. Skills Validation Score (weight: 25%)

Uses TF-IDF to extract the top-N keywords from the job description (N = min(30, total unique JD terms)), then checks each against the resume using both exact match and Levenshtein fuzzy match (distance ≤ 2).

```
matchedSkills   = count of JD keywords found in resume (exact or fuzzy)
totalJdSkills   = total extracted JD keywords (capped at 30)

skillsScore = (matchedSkills / totalJdSkills) × 100
```

Additionally, a **Cosine Similarity bonus** (up to +10 points, capped so total never exceeds 100) is applied:

```
cosineBonus = cosineSimilarity(resume_tfidf_vector, jd_tfidf_vector) × 10
skillsScore = min(100, skillsScore + cosineBonus)
```

---

## 3. Impact & Achievements Score (weight: 20%)

Count bullet points (lines starting with `–`, `-`, `•`, or `*`) that contain at least one:
- Numeric value (e.g., "increased by 40%", "managed 12 engineers")
- Strong action verb from the predefined verb list in ATSScoreEngine

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
| Presence of filler words (e.g., "responsible for", "helped with", "assisted in") — per occurrence | –3, max –15 |
| Repetition: same word (excluding stopwords) appears > 5 times | –5 per repeated word, max –15 |
| Passive voice ratio > 40% of sentences | –10 |

**Floor:** Writing score cannot go below 0.

---

## 6. Professionalism Score (weight: 5%)

**Base score:** 100

| Deduction Rule | Amount |
|---------------|--------|
| Spelling errors detected (simple dictionary check against a curated word list) | –5 per error, max –20 |
| Inconsistent date formatting (mix of "Jan 2023" and "01/2023" styles) | –10 |
| Inconsistent bullet style (mix of `-` and `•` and `*`) | –5 |
| Email address not professional (contains slang, numbers in a way suggestive of casual email) — heuristic | –10 |
| Resume length > 2 pages (estimated at > 700 words for a single-column resume) | –5 |

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
- `description`: Plain, factual description of the issue (no dynamic or invented numbers).
- `fix`: Actionable advice on resolving the deduction.

### 2. Severity Mapping
Severity is derived directly from the deduction amount incurred by the firing rule:
- **HIGH**: deduction of 15 points or more (e.g., keyword density < 30%, first-person language, missing sections >= 2).
- **MEDIUM**: deduction of 8 to 14 points (e.g., missing contact info, multi-column/table layout, graphics present, keyword density 30%–50%, long sentences, passive voice > 40%, inconsistent dates, casual email).
- **LOW**: deduction below 8 points (e.g., no measurable achievements, inconsistent bullets, over 700 words).

### 3. Fired Rules & Definitions
All risk titles, descriptions, and fixes are maintained in a constant map (`RISK_DEFINITIONS`) in `ATSScoreEngine`:
1. **Missing Section Headings**: missing standard headings (Education, Experience, Skills) (–8 to –24).
2. **No Contact Info**: missing email or phone (–10).
3. **Multi-Column or Table Layout**: multi-column or table formatting detected (–10).
4. **Graphics Present**: embedded images or graphics detected (–8).
5. **Keyword Density Below 30 Percent**: JD keyword match < 30% (–15).
6. **Keyword Density Between 30 and 50 Percent**: JD keyword match 30%–50% (–8).
7. **No Measurable Achievements**: no numbers or metric indicators detected (–5).
8. **First-Person Language**: usage of I, me, my, we, our (–15).
9. **Filler Words**: weak phrases like "responsible for", "helped with" (–3 per, max –15).
10. **Repeated Words**: words appearing > 5 times (–5 per, max –15).
11. **Long Sentences**: average sentence length > 35 words (–10).
12. **Passive Voice Over 40 Percent**: passive sentence ratio > 40% (–10).
13. **Spelling Errors**: common misspelled words found (–5 per, max –20).
14. **Inconsistent Dates**: mix of alpha and numeric date styles (–10).
15. **Inconsistent Bullets**: mix of bullet characters (–5).
16. **Casual Email**: informal email pattern (–10).
17. **Over 700 Words**: resume text exceeding 700 words (–5).

### 4. Ordering & Determinism
- Risks are sorted in order of severity: `HIGH` first, then `MEDIUM`, then `LOW`.
- Clean resumes with no triggered deductions produce an empty risks list (`[]`).
- `GET /api/v1/analysis/{id}` recomputes risks deterministically from persisted inputs, guaranteeing identical output to `POST /api/v1/analysis/match`.
- The `hasFormattingRisk` scoring input is persisted as a boolean column on `Resume` alongside `hasGraphicsRisk`.

