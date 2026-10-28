/**
 * InsightCard: Quick insight card displaying metric title, highlighted value,
 * description or subtext, and an optional tier badge or icon.
 *
 * @param {Object} props
 * @param {string} props.title - Metric title or header
 * @param {string|number} props.value - Primary prominent display value
 * @param {string} [props.subtext] - Supporting context or secondary detail
 * @param {string} [props.badgeText] - Optional status badge text
 * @param {string} [props.badgeColor] - Color for badge border and background tint
 * @param {React.ElementType} [props.icon] - Optional lucide-react icon component
 */
export default function InsightCard({
  title,
  value,
  subtext,
  badgeText,
  badgeColor,
  icon: Icon,
}) {
  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col justify-between hover:border-[var(--border-strong)] transition-colors duration-150">
      <div className="flex items-center justify-between mb-3">
        <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
          {title}
        </span>
        {Icon && (
          <div className="w-7 h-7 rounded-[8px] bg-[rgba(232,96,46,0.10)] border border-[rgba(232,96,46,0.20)] flex items-center justify-center text-[var(--brand-500)]">
            <Icon className="w-3.5 h-3.5" aria-hidden="true" />
          </div>
        )}
      </div>

      <div>
        <div className="flex items-baseline gap-2 flex-wrap">
          <span className="font-heading font-bold text-xl sm:text-2xl text-[var(--text)] tracking-tight">
            {value}
          </span>
          {badgeText && (
            <span
              className="text-[11px] font-semibold px-2 py-0.5 rounded-[6px]"
              style={{
                backgroundColor: badgeColor ? `${badgeColor}1F` : 'var(--bg-elevated)',
                color: badgeColor || 'var(--text)',
                border: `1px solid ${badgeColor ? `${badgeColor}40` : 'var(--border)'}`,
              }}
            >
              {badgeText}
            </span>
          )}
        </div>
        {subtext && (
          <p className="text-xs text-[var(--text-secondary)] mt-1.5 leading-relaxed">
            {subtext}
          </p>
        )}
      </div>
    </div>
  );
}
