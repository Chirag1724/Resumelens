import { SCORING_CATEGORIES } from '../lib/scoring';
import { Sparkles } from 'lucide-react';

/**
 * SummaryCard: Displays the summary sentence derived from overall score,
 * tier, strongest and weakest dimensions, high severity risks, and missing keywords.
 *
 * @param {Object} props
 * @param {number} props.overallScore - The overall resume score (0 to 100)
 * @param {string} props.tierLabel - Qualitative tier label (e.g. "Strong Match")
 * @param {Array<Object>} props.sectionScores - Array of section score objects from the API
 * @param {number} props.highRiskCount - Number of high severity risks found
 * @param {number} props.missingKeywordCount - Number of missing keywords found
 * @param {boolean} [props.scoreCapped=false] - Whether the score was capped due to low job match
 */
export default function SummaryCard({
  overallScore = 0,
  tierLabel = 'Match',
  sectionScores = [],
  highRiskCount = 0,
  missingKeywordCount = 0,
  scoreCapped = false,
}) {
  const roundedOverall = Math.round(Number(overallScore) || 0);

  // Map section scores to display names
  const categoryMap = new Map(
    SCORING_CATEGORIES.map((cat) => [cat.sectionName, cat.name])
  );

  const enrichedSections = sectionScores.map((sec) => ({
    name: categoryMap.get(sec.sectionName) || sec.sectionName,
    score: Math.round(Number(sec.score) || 0),
  }));

  let strongest = { name: 'ATS Compatibility', score: 0 };
  let weakest = { name: 'Writing Quality', score: 100 };

  if (enrichedSections.length > 0) {
    strongest = enrichedSections.reduce((best, curr) =>
      curr.score > best.score ? curr : best
    );
    weakest = enrichedSections.reduce((worst, curr) =>
      curr.score < worst.score ? curr : worst
    );
  }

  const n = Math.max(0, Number(highRiskCount) || 0);
  const m = Math.max(0, Number(missingKeywordCount) || 0);

  let findingPhrase = '';
  if (n === 0) {
    if (m === 0) {
      findingPhrase = 'No high severity risks were found.';
    } else {
      const kwPhrase = m === 1 ? '1 missing keyword was found.' : `${m} missing keywords were found.`;
      findingPhrase = `No high severity risks were found. ${kwPhrase}`;
    }
  } else {
    const riskPart = n === 1 ? '1 high severity risk' : `${n} high severity risks`;
    const kwPart = m === 1 ? '1 missing keyword' : `${m} missing keywords`;
    findingPhrase = `${riskPart} and ${kwPart} were found.`;
  }

  const cappedSentence = scoreCapped
    ? ' The overall score is limited because the job match is low.'
    : '';

  const summaryText = `Your resume scored ${roundedOverall} out of 100, rated ${tierLabel}. Strongest area: ${strongest.name} (${strongest.score}). Weakest area: ${weakest.name} (${weakest.score}). ${findingPhrase}${cappedSentence}`;

  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 relative overflow-hidden">
      <div className="flex items-center gap-2 mb-3">
        <div className="w-6 h-6 rounded-[6px] bg-[rgba(232,96,46,0.10)] border border-[rgba(232,96,46,0.20)] flex items-center justify-center text-[var(--brand-500)]">
          <Sparkles className="w-3.5 h-3.5" aria-hidden="true" />
        </div>
        <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
          Summary
        </span>
      </div>

      <p className="text-sm sm:text-base text-[var(--text)] leading-relaxed font-medium">
        {summaryText}
      </p>
    </div>
  );
}
