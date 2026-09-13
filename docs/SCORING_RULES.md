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
| No measurable achievements found (no digits/percentages in bullet points) | –5 | |
| File format not plain-text-parseable (binary/image PDF with no text layer) | –20 | Applied at parse time; short-circuits scoring |

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
- Strong action verb from the predefined verb list (see `util/ActionVerbList.java`)

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

After computing `overallScore`, assign one of the following labels:

| overallScore Range | Status Label |
|--------------------|--------------|
| ≥ 85 | **Excellent Match** |
| 70–84 | **Strong Match** |
| 55–69 | **Moderate Match** |
| 40–54 | **Weak Match** |
| < 40 | **Poor Match** |

---

## Improvement Prompt Generation

After scoring, the system generates a structured copyable prompt. The prompt must include:
1. The job title (if detectable from the JD).
2. The full list of missing JD keywords not found in the resume.
3. The sections with the lowest scores and their feedback strings.
4. A fixed instruction block asking the external AI to rewrite or improve the corresponding resume sections.

The system **does not call any external API** to generate this prompt. It is built by string assembly in `PromptGeneratorService`.
