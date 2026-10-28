/**
 * Scoring categories and weights definition.
 * Dimension names and weights match docs/SCORING_RULES.md.
 * Section names match docs/API_CONTRACT.md.
 */

export const SCORING_CATEGORIES = [
  {
    sectionName: 'ATS_COMPATIBILITY',
    name: 'ATS Compatibility',
    weight: 25,
    weightDecimal: 0.25,
    description: 'Measures standard section headings, contact information, file parsing readiness, and keyword density.',
  },
  {
    sectionName: 'SKILLS_VALIDATION',
    name: 'Skills Validation',
    weight: 25,
    weightDecimal: 0.25,
    description: 'Measures alignment between job description keywords and candidate skills using exact and fuzzy matching.',
  },
  {
    sectionName: 'IMPACT_ACHIEVEMENTS',
    name: 'Impact & Achievements',
    weight: 20,
    weightDecimal: 0.20,
    description: 'Measures the proportion of bullet points containing measurable metrics and action verbs.',
  },
  {
    sectionName: 'PROJECT_QUALITY',
    name: 'Project Quality',
    weight: 15,
    weightDecimal: 0.15,
    description: 'Measures project structure including project title, technical stack mentions, descriptions, and tangible outcomes.',
  },
  {
    sectionName: 'WRITING_QUALITY',
    name: 'Writing Quality',
    weight: 10,
    weightDecimal: 0.10,
    description: 'Measures conciseness, active voice usage, vocabulary repetition, and avoidance of filler phrasing.',
  },
  {
    sectionName: 'PROFESSIONALISM',
    name: 'Professionalism',
    weight: 5,
    weightDecimal: 0.05,
    description: 'Measures formatting consistency, bullet uniformity, date styling, spelling accuracy, and document length.',
  },
];

export default SCORING_CATEGORIES;

