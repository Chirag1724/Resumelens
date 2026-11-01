import { getScoreDetails } from '../lib/score';
import { formatScore } from '../lib/format';

/**
 * SectionDetailCard: Card for an individual scoring dimension featuring a 40px
 * rounded container, numerical score out of 100, tier label badge, thin progress bar,
 * and static educational description of what the dimension measures.
 *
 * @param {Object} props
 * @param {string} props.name - Dimension display name
 * @param {number} props.score - Dimension score (0 to 100)
 * @param {number} [props.weight] - Weight percentage (e.g. 25)
 * @param {string} [props.status] - Status tier string
 * @param {string} props.description - Educational copy describing the dimension
 * @param {React.ElementType} [props.icon] - Lucide icon component
 */
export default function SectionDetailCard({
  name,
  score = 0,
  weight,
  status,
  description,
  icon: Icon,
}) {
  const rawScore = Number(score) || 0;
  const numScore = Math.max(0, Math.min(100, rawScore));
  const scoreInfo = getScoreDetails(status || rawScore);

  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col justify-between hover:border-[var(--border-strong)] transition-colors duration-150">
      <div>
        <div className="flex items-start justify-between gap-3 mb-4">
          {/* 40px rounded icon container */}
          <div className="w-10 h-10 rounded-[12px] bg-[rgba(232,96,46,0.10)] border border-[rgba(232,96,46,0.20)] flex items-center justify-center text-[var(--brand-500)] shrink-0">
            {Icon && <Icon className="w-5 h-5" aria-hidden="true" />}
          </div>

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

        <div className="flex items-baseline justify-between mb-2">
          <h3 className="font-heading font-semibold text-sm text-[var(--text)]">
            {name}
            {weight !== undefined && (
              <span className="text-[11px] font-mono text-[var(--text-tertiary)] ml-1.5 font-normal">
                ({weight}%)
              </span>
            )}
          </h3>
          <span className="font-heading font-bold text-base text-[var(--text)]">
            {formatScore(rawScore)}
            <span className="text-xs font-normal text-[var(--text-tertiary)]"> / 100</span>
          </span>
        </div>

        {/* Thin progress bar */}
        <div className="w-full h-1.5 rounded-full bg-[var(--bg-page)] overflow-hidden border border-[var(--border)] mb-3">
          <div
            className="h-full rounded-full transition-all duration-300 ease-out"
            style={{
              width: `${numScore}%`,
              backgroundColor: scoreInfo.color,
            }}
          />
        </div>

        {/* Fixed explanatory description */}
        <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
          {description}
        </p>
      </div>
    </div>
  );
}
