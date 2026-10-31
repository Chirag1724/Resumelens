import {
  VERDICTS,
  CATEGORIES,
  EFFORT,
  IMPORTANCE,
  RISK_COPY,
} from '../content/reportCopy.js';

function parseKeywordsList(raw) {
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed;
    } catch {
      return raw.split(',').map((s) => s.trim()).filter(Boolean);
    }
  }
  return [];
}

export function tierFor(overallScore) {
  const num = Number(overallScore);
  const score = Number.isFinite(num) ? num : 0;
  if (score >= 85) return 'EXCELLENT';
  if (score >= 70) return 'STRONG';
  if (score >= 55) return 'MODERATE';
  if (score >= 40) return 'WEAK';
  return 'POOR';
}

export function verdictFor(overallScore) {
  const tier = tierFor(overallScore);
  return VERDICTS[tier];
}

export function capInfo(data) {
  if (!data || !data.scoreCapped) {
    return null;
  }

  const cap = Number(data.overallScore) || 0;
  const uncapped = Number(data.uncappedScore) || 0;

  const sections = Array.isArray(data.sectionScores) ? data.sectionScores : [];
  const skillsSection = sections.find((s) => s.sectionName === 'SKILLS_VALIDATION');
  const skillsNow = data.skillsScore !== undefined
    ? Number(data.skillsScore)
    : (skillsSection ? Number(skillsSection.score) || 0 : 0);

  const threshold = skillsNow < 40 ? 40 : 60;
  const needed = threshold - skillsNow;

  const matched = parseKeywordsList(skillsSection?.matchedKeywords);
  const missing = parseKeywordsList(skillsSection?.missingKeywords);
  const totalKeywords = matched.length + missing.length;

  let moreSkills = null;
  if (totalKeywords > 0) {
    const perKeyword = 100 / totalKeywords;
    const calculated = Math.ceil(needed / perKeyword);
    if (calculated <= missing.length) {
      moreSkills = Math.max(0, calculated);
    }
  }

  return {
    cap,
    uncapped,
    threshold,
    skillsNow,
    moreSkills,
  };
}

export function pointsGain(risk) {
  const points = Number(risk?.points) || 0;
  const weight = CATEGORIES[risk?.section]?.weight || 0;
  const gain = (points * weight) / 100;
  return Number((Math.round((gain + Number.EPSILON) * 10) / 10).toFixed(1));
}

export function pointsEarned(sectionScore, weight) {
  const score = Number(sectionScore) || 0;
  const w = Number(weight) || 0;
  const earned = (score * w) / 100;
  const rounded = (Math.round((earned + Number.EPSILON) * 10) / 10).toFixed(1);
  return `${rounded} of ${w}`;
}

export function keywordBand(percent) {
  const p = Number(percent) || 0;
  if (p >= 80) return 'great';
  if (p >= 50) return 'decent';
  return 'low';
}

export function summaryFacts(data) {
  const sections = Array.isArray(data?.sectionScores) ? data.sectionScores : [];
  const skillsSection = sections.find((s) => s.sectionName === 'SKILLS_VALIDATION');
  const matched = parseKeywordsList(skillsSection?.matchedKeywords);
  const missing = parseKeywordsList(skillsSection?.missingKeywords);
  const totalKeywords = matched.length + missing.length;
  const percent = totalKeywords > 0
    ? Math.round((matched.length / totalKeywords) * 100)
    : Math.round(Number(data?.keywordMatchPercent) || 0);

  const risks = Array.isArray(data?.risks) ? data.risks : [];
  const riskCount = risks.length;
  const highRiskCount = risks.filter((r) => r.severity === 'HIGH').length;

  const categoryOrder = Object.keys(CATEGORIES);
  let strongestSection = categoryOrder[0];
  let bestScore = -Infinity;
  let weakestSection = categoryOrder[0];
  let worstScore = Infinity;

  for (const catKey of categoryOrder) {
    const section = sections.find((s) => s.sectionName === catKey);
    const score = section ? Number(section.score) || 0 : 0;
    if (score > bestScore) {
      bestScore = score;
      strongestSection = catKey;
    }
    if (score < worstScore) {
      worstScore = score;
      weakestSection = catKey;
    }
  }

  return {
    skillsCovered: `${matched.length} of ${totalKeywords} skills`,
    percent,
    keywordPercent: percent,
    riskCount,
    totalRisks: riskCount,
    highRiskCount,
    strongestCategory: CATEGORIES[strongestSection]?.label || strongestSection,
    weakestCategory: CATEGORIES[weakestSection]?.label || weakestSection,
    strongestSection,
    weakestSection,
    strongestScore: bestScore === -Infinity ? 0 : bestScore,
    weakestScore: worstScore === Infinity ? 0 : worstScore,
  };
}

export function buildActionPlan(data) {
  if (!data) return [];

  const plan = [];
  const isCapped = Boolean(data.scoreCapped);
  const cInfo = capInfo(data);

  const sections = Array.isArray(data.sectionScores) ? data.sectionScores : [];
  const skillsSection = sections.find((s) => s.sectionName === 'SKILLS_VALIDATION');
  const missingList = parseKeywordsList(skillsSection?.missingKeywords);

  const risks = Array.isArray(data.risks) ? data.risks : [];
  const suggestions = Array.isArray(data.suggestions) ? data.suggestions : [];

  // 1. Cap item when capped
  if (isCapped && cInfo) {
    const moreText = cInfo.moreSkills !== null ? `${cInfo.moreSkills} or so ` : '';
    plan.push({
      id: 'cap',
      title: 'Match the job better',
      why: `Your score is held at ${cInfo.cap} because the match with the job is low.`,
      how: `Add the ${moreText}missing skills you genuinely have, inside your bullets.`,
      effort: EFFORT.CONTENT,
      importance: IMPORTANCE.HIGH,
      gainText: cInfo.uncapped > cInfo.cap
        ? `Could raise your overall score by up to ${Number((cInfo.uncapped - cInfo.cap).toFixed(1))} points`
        : '',
      example: null,
      fromResume: null,
    });
  }

  // 2. Skills item (only when missing list not empty and no cap item exists)
  const keywordsSuggestion = suggestions.find((s) => s.code === 'KEYWORDS');
  const densityRisk = risks.find(
    (r) => r.code === 'KEYWORD_DENSITY_BELOW_30' || r.code === 'KEYWORD_DENSITY_30_TO_50'
  );

  if (!isCapped && missingList.length > 0 && (keywordsSuggestion || densityRisk)) {
    const riskCopy = densityRisk ? RISK_COPY[densityRisk.code] : null;
    const gainText = densityRisk
      ? `Could raise your overall score by up to ${pointsGain(densityRisk)} points`
      : '';
    const importance = densityRisk
      ? (IMPORTANCE[densityRisk.severity] || IMPORTANCE.HIGH)
      : (IMPORTANCE[keywordsSuggestion?.priority] || IMPORTANCE.HIGH);

    plan.push({
      id: 'skills',
      title: 'Add the skills this job asks for',
      why: riskCopy?.why || 'Screening often starts by matching skills. A low match can push your resume down the list.',
      how: riskCopy?.fix || 'Add the missing skills you genuinely have, inside your experience and project bullets.',
      effort: EFFORT.CONTENT,
      importance,
      gainText,
      example: riskCopy?.example || null,
      fromResume: keywordsSuggestion?.example || null,
    });
  }

  // 3. Risks by severity (HIGH, MEDIUM, LOW), within severity by points descending, excluding keyword density risks
  const nonKeywordRisks = risks.filter(
    (r) => r.code !== 'KEYWORD_DENSITY_BELOW_30' && r.code !== 'KEYWORD_DENSITY_30_TO_50'
  );

  const severityRank = { HIGH: 1, MEDIUM: 2, LOW: 3 };
  const sortedRisks = [...nonKeywordRisks].sort((a, b) => {
    const rankA = severityRank[a.severity] || 99;
    const rankB = severityRank[b.severity] || 99;
    if (rankA !== rankB) return rankA - rankB;
    const pointsA = Number(a.points) || 0;
    const pointsB = Number(b.points) || 0;
    return pointsB - pointsA;
  });

  for (const risk of sortedRisks) {
    const copy = RISK_COPY[risk.code];
    const title = copy ? copy.title : risk.title;
    const why = copy ? copy.why : risk.description;
    const how = copy ? copy.fix : risk.fix;
    const effort = copy ? (EFFORT[copy.effort] || copy.effort) : EFFORT.QUICK;
    const importance = IMPORTANCE[risk.severity] || risk.severity || 'Medium importance';
    const example = copy?.example || null;
    const sectionLabel = CATEGORIES[risk.section]?.label || risk.section;
    const points = Number(risk.points) || 0;
    const gainText = isCapped
      ? `Raises your ${sectionLabel} score by up to ${points} points`
      : `Could raise your overall score by up to ${pointsGain(risk)} points`;

    plan.push({
      id: risk.code || `risk-${risk.title}`,
      title,
      why,
      how,
      effort,
      importance,
      gainText,
      example,
      fromResume: null,
      _code: risk.code,
    });
  }

  // 4. Suggestions whose code is not already covered
  const COVERED_BY = {
    QUANTIFY: 'NO_MEASURABLE_ACHIEVEMENTS',
    STRUCTURE: 'MISSING_SECTIONS',
    WEAK_VERBS: 'FILLER_WORDS',
  };

  for (const suggestion of suggestions) {
    if (suggestion.code === 'KEYWORDS') {
      continue;
    }
    const coveringRiskCode = COVERED_BY[suggestion.code];
    if (coveringRiskCode) {
      const targetItem = plan.find((item) => item._code === coveringRiskCode || item.id === coveringRiskCode);
      if (targetItem) {
        targetItem.fromResume = suggestion.example || null;
        continue;
      }
    }

    plan.push({
      id: suggestion.code || `suggestion-${suggestion.title}`,
      title: suggestion.title,
      why: suggestion.description,
      how: suggestion.description,
      effort: EFFORT.QUICK,
      importance: IMPORTANCE[suggestion.priority] || suggestion.priority || 'Medium importance',
      gainText: '',
      example: null,
      fromResume: suggestion.example || null,
      _code: suggestion.code,
    });
  }

  return plan.slice(0, 6).map((item) => {
    const { _code, ...cleanItem } = item;
    return cleanItem;
  });
}

export default {
  tierFor,
  verdictFor,
  capInfo,
  pointsGain,
  pointsEarned,
  keywordBand,
  summaryFacts,
  buildActionPlan,
};

