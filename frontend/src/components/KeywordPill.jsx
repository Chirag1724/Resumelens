/**
 * KeywordPill: Displays a categorized keyword pill for matched (green-tinted)
 * or missing (orange-tinted) skills without individual percentage metrics.
 *
 * @param {Object} props
 * @param {string} props.keyword - Keyword text to display
 * @param {'matched'|'missing'} [props.variant='matched'] - Visual status variant
 */
export default function KeywordPill({ keyword, variant = 'matched' }) {
  const isMatched = variant === 'matched';

  return (
    <span
      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium transition-colors select-none ${
        isMatched
          ? 'bg-[rgba(0,164,109,0.12)] text-[var(--success)] border border-[rgba(0,164,109,0.28)]'
          : 'bg-[rgba(255,155,0,0.12)] text-[var(--accent-orange)] border border-[rgba(255,155,0,0.28)]'
      }`}
    >
      {keyword}
    </span>
  );
}
