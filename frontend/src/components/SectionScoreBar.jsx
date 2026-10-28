import { getScoreDetails } from '../lib/score';

/**
 * SectionScoreBar: Horizontal progress bar for a scoring dimension showing
 * name, score out of 100, weight percentage, status pill, and concise rule feedback.
 *
 * @param {Object} props
 * @param {string} props.sectionName - Identifier key of the section (e.g. "ATS_COMPATIBILITY")
 * @param {string} props.name - Human-readable category title
 * @param {number} props.score - Numerical score (0 to 100)
 * @param {number} props.weight - Dimension weight percentage (e.g. 25)
 * @param {string} [props.status] - Dimension status string (e.g. "STRONG", "EXCELLENT")
 * @param {string} [props.feedback] - Concise rule feedback from analysis
 */
export default function SectionScoreBar({
  name,
  score = 0,
  weight,
  status,
  feedback,
}) {
  const numScore = Math.max(0, Math.min(100, Math.round(Number(score) || 0)));
  const scoreInfo = getScoreDetails(status || numScore);

  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-3">
        <div className="flex items-center gap-2">
          <span className="font-heading font-semibold text-sm text-[var(--text)]">
            {name}
          </span>
          {weight !== undefined && (
            <span className="text-[11px] font-mono text-[var(--text-tertiary)]">
              ({weight}%)
            </span>
          )}
        </div>

        <div className="flex items-center gap-3">
          <span className="font-heading font-bold text-sm text-[var(--text)]">
            {numScore}
            <span className="text-xs font-normal text-[var(--text-tertiary)]"> / 100</span>
          </span>
          <span
            className="text-[11px] font-semibold px-2 py-0.5 rounded-[6px]"
            style={{
              backgroundColor: `${scoreInfo.color}1F`,
              color: scoreInfo.color,
              border: `1px solid ${scoreInfo.color}40`,
            }}
          >
            {scoreInfo.label}
          </span>
        </div>
      </div>

      {/* Progress track and animated fill */}
      <div className="w-full h-2 rounded-full bg-[var(--bg-page)] overflow-hidden border border-[var(--border)]">
        <div
          className="h-full rounded-full transition-all duration-300 ease-out"
          style={{
            width: `${numScore}%`,
            backgroundColor: scoreInfo.color,
          }}
          role="progressbar"
          aria-valuenow={numScore}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label={`${name} score`}
        />
      </div>

      {/* Concise rule feedback */}
      {feedback && (
        <p className="text-xs text-[var(--text-secondary)] mt-2 leading-relaxed">
          {feedback}
        </p>
      )}
    </div>
  );
}
