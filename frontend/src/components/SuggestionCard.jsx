import { Lightbulb } from 'lucide-react';

/**
 * SuggestionCard: Actionable recommendation card displaying priority badge,
 * suggestion title, clear description, and a concrete fix example.
 *
 * @param {Object} props
 * @param {'HIGH'|'MEDIUM'|'LOW'} [props.priority='MEDIUM'] - Suggestion priority level
 * @param {string} props.title - Actionable suggestion title
 * @param {string} props.description - Detailed rationale and instruction
 * @param {string} [props.example] - Concrete before/after or suggested phrasing example
 */
export default function SuggestionCard({
  priority = 'MEDIUM',
  title,
  description,
  example,
}) {
  const normalizedPriority = (priority || 'MEDIUM').toUpperCase();

  const priorityStyles = {
    HIGH: {
      color: 'var(--risk-high)',
      bg: 'rgba(239, 68, 68, 0.12)',
      border: 'rgba(239, 68, 68, 0.28)',
      label: 'High Priority',
    },
    MEDIUM: {
      color: 'var(--risk-medium)',
      bg: 'rgba(251, 201, 4, 0.12)',
      border: 'rgba(251, 201, 4, 0.28)',
      label: 'Medium Priority',
    },
    LOW: {
      color: 'var(--risk-low)',
      bg: 'rgba(153, 153, 153, 0.12)',
      border: 'rgba(153, 153, 153, 0.28)',
      label: 'Low Priority',
    },
  }[normalizedPriority] || {
    color: 'var(--risk-low)',
    bg: 'rgba(153, 153, 153, 0.12)',
    border: 'rgba(153, 153, 153, 0.28)',
    label: normalizedPriority,
  };

  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col justify-between hover:border-[var(--border-strong)] transition-colors duration-150">
      <div>
        <div className="flex items-center justify-between gap-2 mb-3">
          <span
            className="text-[11px] font-semibold px-2 py-0.5 rounded-[6px]"
            style={{
              backgroundColor: priorityStyles.bg,
              color: priorityStyles.color,
              border: `1px solid ${priorityStyles.border}`,
            }}
          >
            {priorityStyles.label}
          </span>
          <Lightbulb className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
        </div>

        <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-2">
          {title}
        </h3>

        <p className="text-xs text-[var(--text-secondary)] leading-relaxed mb-3">
          {description}
        </p>
      </div>

      {example && (
        <div className="mt-2 p-2.5 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)] text-xs font-mono text-[var(--text-secondary)]">
          <span className="text-[11px] font-semibold text-[var(--text)] uppercase tracking-wider block mb-1">
            Example
          </span>
          <p className="text-xs text-[var(--text-secondary)] break-words leading-relaxed">
            {example}
          </p>
        </div>
      )}
    </div>
  );
}
