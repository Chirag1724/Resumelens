import {
  VERDICTS,
  QUALITY_ONLY_VERDICT,
  CATEGORIES,
  EFFORT,
  IMPORTANCE,
  RISK_COPY,
  SUGGESTION_COPY,
} from '../content/reportCopy.js';
import { formatScore } from './format.js';
import { prettySkill } from './skillNames.js';

export { QUALITY_ONLY_VERDICT };

export const SCALE_SEGMENT_WIDTHS = [40, 15, 15, 15, 15];
export const SCALE_WIDTHS = SCALE_SEGMENT_WIDTHS;
export const TIER_WIDTHS = {
  POOR: 40,
  WEAK: 15,
  MODERATE: 15,
  STRONG: 15,
  EXCELLENT: 15,
};

export function scaleMarkerPosition(score) {
  const num = Number(score);
  const valid = Number.isFinite(num) ? num : 0;
  return Math.max(0, Math.min(100, valid));
}
export const getMarkerPosition = scaleMarkerPosition;
export const markerPosition = scaleMarkerPosition;

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

export function areKeywordListsEmpty(dataOrMatched, maybeMissing) {
  if (typeof dataOrMatched === 'boolean') {
    return dataOrMatched;
  }
  if (
    Array.isArray(dataOrMatched) ||
    Array.isArray(maybeMissing) ||
    (maybeMissing !== undefined && (typeof dataOrMatched === 'string' || typeof maybeMissing === 'string'))
  ) {
    const matched = parseKeywordsList(dataOrMatched);
    const missing = parseKeywordsList(maybeMissing);
    return matched.length === 0 && missing.length === 0;
  }
  if (dataOrMatched && typeof dataOrMatched === 'object') {
    if (typeof dataOrMatched.isKeywordListEmpty === 'boolean') {
      return dataOrMatched.isKeywordListEmpty;
    }
    const sections = Array.isArray(dataOrMatched.sectionScores) ? dataOrMatched.sectionScores : [];
    const skillsSection = sections.find((s) => s.sectionName === 'SKILLS_VALIDATION');
    const matchedRaw = skillsSection ? skillsSection.matchedKeywords : dataOrMatched.matchedKeywords;
    const missingRaw = skillsSection ? skillsSection.missingKeywords : dataOrMatched.missingKeywords;
    const matched = parseKeywordsList(matchedRaw);
    const missing = parseKeywordsList(missingRaw);
    return matched.length === 0 && missing.length === 0;
  }
  return false;
}

export function verdictFor(overallScoreOrData, matchedOrOptionsOrEmpty, missing) {
  let score = 0;
  let isQualityOnly = false;

  if (typeof overallScoreOrData === 'object' && overallScoreOrData !== null) {
    score = overallScoreOrData.overallScore !== undefined ? Number(overallScoreOrData.overallScore) : 0;
    isQualityOnly = areKeywordListsEmpty(overallScoreOrData);
  } else {
    score = Number(overallScoreOrData) || 0;
    if (matchedOrOptionsOrEmpty !== undefined) {
      isQualityOnly = areKeywordListsEmpty(matchedOrOptionsOrEmpty, missing);
    }
  }

  if (isQualityOnly) {
    return QUALITY_ONLY_VERDICT;
  }

  const tier = tierFor(score);
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
  return `${formatScore(earned)} of ${formatScore(w)}`;
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
  const isKeywordListEmpty = matched.length === 0 && missing.length === 0;
  const percent = isKeywordListEmpty
    ? null
    : (totalKeywords > 0
        ? Math.round((matched.length / totalKeywords) * 100)
        : Math.round(Number(data?.keywordMatchPercent) || 0));

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
    skillsCovered: isKeywordListEmpty ? 'Not scored' : `${matched.length} of ${totalKeywords} skills`,
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
    const why = `Your score is held at ${formatScore(cInfo.cap)} because your skills match is below ${formatScore(cInfo.threshold)}.`;
    const how = cInfo.moreSkills !== null
      ? `Add about ${cInfo.moreSkills} more of the missing skills you genuinely have, inside your bullets.`
      : 'Add the missing skills you genuinely have, inside your bullets.';
    const gainText = cInfo.threshold === 60
      ? `Could lift your score from ${formatScore(cInfo.cap)} to about ${formatScore(cInfo.uncapped)}`
      : `Could lift your score from ${formatScore(cInfo.cap)} to up to 69`;

    plan.push({
      id: 'cap',
      title: 'Match the job better',
      why,
      how,
      effort: EFFORT.CONTENT,
      importance: IMPORTANCE.HIGH,
      gainText,
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
      ? `Could raise your overall score by up to ${formatScore(pointsGain(densityRisk))} points`
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
      fromResume: null,
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
    let how = copy ? copy.fix : risk.fix;
    if (risk.code === 'REPEATED_WORDS') {
      how = `${how} Names of organizations or tools can repeat naturally. You can ignore those.`.trim();
    }
    const effort = copy ? (EFFORT[copy.effort] || copy.effort) : EFFORT.QUICK;
    const importance = IMPORTANCE[risk.severity] || risk.severity || 'Medium importance';
    const example = copy?.example || null;
    const sectionLabel = CATEGORIES[risk.section]?.label || risk.section;
    const points = Number(risk.points) || 0;
    const gainText = isCapped
      ? `Raises your ${sectionLabel} score by up to ${formatScore(points)} points`
      : `Could raise your overall score by up to ${formatScore(pointsGain(risk))} points`;

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
        if (suggestion.code === 'WEAK_VERBS') {
          targetItem.fromResume = suggestion.example || null;
        } else if (suggestion.code === 'QUANTIFY') {
          targetItem.fromResume = null;
          targetItem.example = SUGGESTION_COPY.QUANTIFY.example;
          targetItem.exampleLabel = 'Example';
        } else if (suggestion.code === 'STRUCTURE') {
          targetItem.fromResume = null;
          if (targetItem.example) {
            targetItem.exampleLabel = 'Example';
          }
        }
        continue;
      }
    }

    const sCopy = SUGGESTION_COPY[suggestion.code];
    if (suggestion.code === 'WEAK_VERBS') {
      plan.push({
        id: 'WEAK_VERBS',
        title: sCopy.title,
        why: sCopy.why,
        how: sCopy.how,
        effort: EFFORT[sCopy.effort] || sCopy.effort,
        importance: IMPORTANCE[suggestion.priority] || IMPORTANCE.HIGH,
        gainText: '',
        example: null,
        fromResume: suggestion.example || null,
        _code: 'WEAK_VERBS',
      });
    } else if (suggestion.code === 'QUANTIFY') {
      const impactSection = sections.find((s) => s.sectionName === 'IMPACT_ACHIEVEMENTS');
      const impactScore = impactSection ? Number(impactSection.score) || 0 : (Number(data.impactScore) || 0);

      let n = null;
      if (suggestion.description && typeof suggestion.description === 'string') {
        const match = suggestion.description.trim().match(/^(\d+)/);
        if (match) {
          n = parseInt(match[1], 10);
        }
      }

      let why = sCopy.why;
      if (n !== null) {
        const bulletSentence = n === 1 ? '1 of your bullets has no number.' : `${n} of your bullets have no number.`;
        why = `${why} ${bulletSentence}`;
      }
      let importance = IMPORTANCE[suggestion.priority] || IMPORTANCE.MEDIUM;
      if (impactScore >= 85) {
        importance = IMPORTANCE.LOW;
        why = `Optional. Your bullets already show clear results, so this is polish. ${why}`;
      }

      plan.push({
        id: 'QUANTIFY',
        title: sCopy.title,
        why,
        how: sCopy.how,
        effort: EFFORT[sCopy.effort] || sCopy.effort,
        importance,
        gainText: '',
        example: sCopy.example,
        exampleLabel: 'Example',
        fromResume: null,
        _code: 'QUANTIFY',
      });
    } else if (suggestion.code === 'STRUCTURE') {
      plan.push({
        id: 'STRUCTURE',
        title: sCopy.title,
        why: sCopy.why,
        how: sCopy.how,
        effort: EFFORT[sCopy.effort] || sCopy.effort,
        importance: IMPORTANCE[suggestion.priority] || IMPORTANCE.QUICK || 'Quick fix',
        gainText: '',
        example: sCopy.example || null,
        exampleLabel: 'Example',
        fromResume: null,
        _code: 'STRUCTURE',
      });
    } else {
      // Unknown suggestion code
      // Rule: "A why and a how must never be the same sentence; if the copy is missing for a code, show only one of them."
      plan.push({
        id: suggestion.code || `suggestion-${suggestion.title}`,
        title: suggestion.title,
        why: suggestion.description || '',
        how: '',
        effort: EFFORT.QUICK,
        importance: IMPORTANCE[suggestion.priority] || suggestion.priority || 'Medium importance',
        gainText: '',
        example: null,
        fromResume: null,
        _code: suggestion.code,
      });
    }
  }

  return plan.slice(0, 6).map((item) => {
    const { _code, ...cleanItem } = item;
    return cleanItem;
  });
}

export default {
  SCALE_SEGMENT_WIDTHS,
  SCALE_WIDTHS,
  TIER_WIDTHS,
  scaleMarkerPosition,
  getMarkerPosition,
  markerPosition,
  tierFor,
  verdictFor,
  capInfo,
  pointsGain,
  pointsEarned,
  keywordBand,
  summaryFacts,
  buildActionPlan,
  areKeywordListsEmpty,
  QUALITY_ONLY_VERDICT,
};
