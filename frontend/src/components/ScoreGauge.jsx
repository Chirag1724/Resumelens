import { useId } from 'react';
import { getScoreDetails } from '../lib/score';

/**
 * ScoreGauge component
 * Displays an SVG score ring with a gradient stroke, soft radial glow, numeric readout, and tier label.
 * Shared component reused on both landing page and report page.
 *
 * @param {Object} props
 * @param {number} props.score - Overall score value from 0 to 100
 * @param {string} [props.tierLabel] - Optional tier label override (e.g. "Strong Match")
 * @param {number} [props.size=170] - Gauge diameter in pixels
 * @param {boolean} [props.showLabel=true] - Whether to show the tier label badge
 * @param {boolean} [props.scoreCapped=false] - Whether the score was capped due to low job match
 * @param {number} [props.uncappedScore] - The raw uncapped score value
 * @param {string} [props.className] - Optional container CSS classes
 */
export default function ScoreGauge({
  score = 0,
  tierLabel,
  size = 170,
  showLabel = true,
  scoreCapped = false,
  uncappedScore,
  className = '',
}) {
  const gradientId = useId();
  const numScore = Math.max(0, Math.min(100, Math.round(Number(score) || 0)));
  const scoreInfo = getScoreDetails(numScore);
  const displayLabel = tierLabel || scoreInfo.label;

  const strokeWidth = 11;
  const radius = (size - strokeWidth * 2) / 2;
  const center = size / 2;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (numScore / 100) * circumference;

  return (
    <div className={`flex flex-col items-center justify-center ${className}`}>
      {/* Gauge SVG container with soft radial glow */}
      <div
        className="relative flex items-center justify-center rounded-full"
        style={{
          width: size,
          height: size,
          boxShadow: '0 0 35px rgba(232, 96, 46, 0.16)',
        }}
      >
        <svg
          width={size}
          height={size}
          viewBox={`0 0 ${size} ${size}`}
          className="transform -rotate-90"
          aria-hidden="true"
        >
          <defs>
            <linearGradient id={gradientId} x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stopColor="var(--brand-400)" />
              <stop offset="100%" stopColor="var(--brand-500)" />
            </linearGradient>
          </defs>

          {/* Background track ring */}
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="transparent"
            stroke="var(--border)"
            strokeWidth={strokeWidth}
          />

          {/* Animated score ring */}
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="transparent"
            stroke={`url(#${gradientId})`}
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            style={{
              transition: 'stroke-dashoffset 500ms ease-out',
            }}
          />
        </svg>

        {/* Center score readout */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center select-none">
          <span
            className="font-heading font-extrabold text-[var(--text)] tracking-tight leading-none"
            style={{ fontSize: '48px' }}
          >
            {numScore}
          </span>
          <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-tertiary)] mt-1">
            out of 100
          </span>
        </div>
      </div>

      {/* Tier label badge */}
      {showLabel && (
        <div
          className="mt-3 px-3 py-1 rounded-[6px] text-xs font-semibold tracking-wide"
          style={{
            backgroundColor: `${scoreInfo.color}1F`,
            color: scoreInfo.color,
            border: `1px solid ${scoreInfo.color}40`,
          }}
        >
          {displayLabel}
        </div>
      )}

      {/* Capped score notice */}
      {Boolean(scoreCapped) && (
        <p className="mt-3 text-xs text-[var(--text-secondary)] text-center max-w-xs leading-relaxed">
          Score limited because the job match is low. Without the limit: {uncappedScore}
        </p>
      )}
    </div>
  );
}
