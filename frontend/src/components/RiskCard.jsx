import { useState } from 'react';
import { ChevronDown, AlertCircle } from 'lucide-react';

/**
 * RiskCard: Expandable compliance deduction item showing severity badge
 * (HIGH, MEDIUM, LOW), risk title, factual description, and actionable fix.
 *
 * @param {Object} props
 * @param {string} props.title - Risk title / fired rule name
 * @param {'HIGH'|'MEDIUM'|'LOW'} props.severity - Severity level of the risk
 * @param {string} props.description - Factual description of the issue
 * @param {string} props.fix - Actionable fix recommendation
 * @param {boolean} [props.defaultExpanded=false] - Initial expansion state
 */
export default function RiskCard({
  title,
  severity = 'LOW',
  description,
  fix,
  defaultExpanded = false,
}) {
  const [isExpanded, setIsExpanded] = useState(defaultExpanded);

  const normalizedSeverity = (severity || 'LOW').toUpperCase();

  const severityStyles = {
    HIGH: {
      color: 'var(--risk-high)',
      bg: 'rgba(239, 68, 68, 0.12)',
      border: 'rgba(239, 68, 68, 0.28)',
      label: 'High Risk',
    },
    MEDIUM: {
      color: 'var(--risk-medium)',
      bg: 'rgba(251, 201, 4, 0.12)',
      border: 'rgba(251, 201, 4, 0.28)',
      label: 'Medium Risk',
    },
    LOW: {
      color: 'var(--risk-low)',
      bg: 'rgba(153, 153, 153, 0.12)',
      border: 'rgba(153, 153, 153, 0.28)',
      label: 'Low Risk',
    },
  }[normalizedSeverity] || {
    color: 'var(--risk-low)',
    bg: 'rgba(153, 153, 153, 0.12)',
    border: 'rgba(153, 153, 153, 0.28)',
    label: normalizedSeverity,
  };

  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] overflow-hidden transition-colors hover:border-[var(--border-strong)]">
      <button
        type="button"
        onClick={() => setIsExpanded((prev) => !prev)}
        aria-expanded={isExpanded}
        className="w-full p-4 flex items-center justify-between gap-3 text-left cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
      >
        <div className="flex items-center gap-3 min-w-0">
          <span
            className="text-[11px] font-semibold px-2 py-0.5 rounded-[6px] shrink-0"
            style={{
              backgroundColor: severityStyles.bg,
              color: severityStyles.color,
              border: `1px solid ${severityStyles.border}`,
            }}
          >
            {severityStyles.label}
          </span>
          <span className="font-heading font-semibold text-sm text-[var(--text)] truncate">
            {title}
          </span>
        </div>

        <ChevronDown
          className={`w-4 h-4 text-[var(--text-tertiary)] shrink-0 transition-transform duration-200 ${
            isExpanded ? 'rotate-180' : ''
          }`}
          aria-hidden="true"
        />
      </button>

      {isExpanded && (
        <div className="px-4 pb-4 pt-1 border-t border-[var(--border)] text-xs text-[var(--text-secondary)] space-y-2.5">
          <p className="leading-relaxed">{description}</p>
          {fix && (
            <div className="p-3 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)] flex items-start gap-2 text-[var(--text)]">
              <AlertCircle className="w-4 h-4 text-[var(--brand-500)] shrink-0 mt-0.5" aria-hidden="true" />
              <div>
                <span className="font-semibold text-xs text-[var(--text)]">Recommended fix: </span>
                <span className="text-xs text-[var(--text-secondary)]">{fix}</span>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
