import { useState } from 'react';
import { RISK_COPY, IMPORTANCE, EFFORT } from '../content/reportCopy';
import {
  ChevronDown,
  AlertCircle,
  AlertTriangle,
  Info,
  Clock,
  Sparkles,
} from 'lucide-react';

const SEVERITY_GROUPS = [
  {
    key: 'HIGH',
    label: 'Important',
    icon: AlertCircle,
    color: 'var(--risk-high)',
    bg: 'rgba(239, 68, 68, 0.1)',
    border: 'rgba(239, 68, 68, 0.25)',
  },
  {
    key: 'MEDIUM',
    label: 'Worth fixing',
    icon: AlertTriangle,
    color: 'var(--risk-medium)',
    bg: 'rgba(251, 201, 4, 0.1)',
    border: 'rgba(251, 201, 4, 0.25)',
  },
  {
    key: 'LOW',
    label: 'Small polish',
    icon: Info,
    color: 'var(--risk-low)',
    bg: 'rgba(153, 153, 153, 0.1)',
    border: 'rgba(153, 153, 153, 0.25)',
  },
];

export default function ThingsToFixSection({ data }) {
  const risks = Array.isArray(data?.risks) ? data.risks : [];

  // HIGH items start expanded
  const [expandedMap, setExpandedMap] = useState(() => {
    const initial = {};
    risks.forEach((risk, idx) => {
      const key = `${risk.code || 'risk'}-${idx}`;
      if (String(risk.severity).toUpperCase() === 'HIGH') {
        initial[key] = true;
      }
    });
    return initial;
  });

  const toggleAccordion = (key) => {
    setExpandedMap((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const hasAnyRisks = risks.length > 0;

  return (
    <section id="issues" aria-labelledby="issues-heading" className="scroll-mt-36">
      {/* Section Header */}
      <div className="mb-5 space-y-1.5">
        <span className="text-[13px] font-semibold text-[var(--brand-500)] tracking-wide">
          Things to fix
        </span>
        <h2
          id="issues-heading"
          className="text-2xl sm:text-[28px] font-heading font-bold text-[var(--text)] leading-tight"
        >
          Things to fix
        </h2>
        <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6] max-w-[60ch]">
          These are the same items as the action plan, with more detail.
        </p>
      </div>

      {!hasAnyRisks ? (
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 sm:p-6 text-center">
          <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6]">
            We found no layout, writing or formatting problems.
          </p>
        </div>
      ) : (
        <div className="space-y-6">
          {SEVERITY_GROUPS.map((group) => {
            const groupRisks = risks.filter(
              (r) => String(r.severity).toUpperCase() === group.key
            );
            if (groupRisks.length === 0) return null;

            const GroupIcon = group.icon;

            return (
              <div key={group.key} className="space-y-3">
                <div className="flex items-center gap-2 text-sm font-semibold text-[var(--text)]">
                  <span
                    className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-[6px] text-xs font-semibold"
                    style={{
                      color: group.color,
                      backgroundColor: group.bg,
                      border: `1px solid ${group.border}`,
                    }}
                  >
                    <GroupIcon className="w-3.5 h-3.5" aria-hidden="true" />
                    <span>{group.label}</span>
                  </span>
                  <span className="text-xs text-[var(--text-tertiary)]">
                    ({groupRisks.length} {groupRisks.length === 1 ? 'issue' : 'issues'})
                  </span>
                </div>

                <div className="space-y-2.5">
                  {groupRisks.map((risk, idx) => {
                    const key = `${risk.code || 'risk'}-${idx}`;
                    const isOpen = Boolean(expandedMap[key]);
                    const copy = RISK_COPY[risk.code];
                    const title = copy?.title || risk.title || 'Formatting issue';
                    const importanceLabel = IMPORTANCE[risk.severity] || risk.severity || group.label;
                    const rawEffort = copy?.effort ? EFFORT[copy.effort] || copy.effort : 'Quick fix';

                    // Append API description when code is REPEATED_WORDS
                    let whatFound = copy?.what || risk.description || '';
                    if (risk.code === 'REPEATED_WORDS' && risk.description) {
                      whatFound = `${whatFound} ${risk.description}`;
                    }

                    const whyMatters = copy?.why || '';
                    const howToFix = copy?.fix || risk.fix || '';
                    const example = copy?.example;

                    return (
                      <div
                        key={key}
                        className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] overflow-hidden transition-all"
                      >
                        {/* Real button for accordion header */}
                        <button
                          type="button"
                          onClick={() => toggleAccordion(key)}
                          aria-expanded={isOpen}
                          aria-controls={`panel-${key}`}
                          id={`header-${key}`}
                          className="w-full p-4 sm:p-5 flex items-center justify-between gap-3 text-left hover:bg-[var(--bg-elevated)] transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
                        >
                          <div className="flex flex-wrap items-center gap-2.5 min-w-0">
                            <h3 className="text-sm sm:text-base font-heading font-bold text-[var(--text)]">
                              {title}
                            </h3>

                            <div className="flex items-center gap-2 text-xs">
                              <span
                                className="inline-flex items-center gap-1 px-2 py-0.5 rounded-[4px] text-[11px] font-medium"
                                style={{
                                  color: group.color,
                                  backgroundColor: group.bg,
                                  border: `1px solid ${group.border}`,
                                }}
                              >
                                <GroupIcon className="w-3 h-3" aria-hidden="true" />
                                <span>{importanceLabel}</span>
                              </span>

                              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-[4px] bg-[var(--bg-page)] border border-[var(--border)] text-[var(--text-secondary)] text-[11px]">
                                <Clock className="w-3 h-3 text-[var(--text-tertiary)]" aria-hidden="true" />
                                <span>{rawEffort}</span>
                              </span>
                            </div>
                          </div>

                          <ChevronDown
                            className={`w-4 h-4 text-[var(--text-tertiary)] shrink-0 transition-transform duration-200 ${
                              isOpen ? 'rotate-180' : ''
                            }`}
                            aria-hidden="true"
                          />
                        </button>

                        {/* Accordion Panel */}
                        {isOpen && (
                          <div
                            id={`panel-${key}`}
                            role="region"
                            aria-labelledby={`header-${key}`}
                            className="p-4 sm:p-5 pt-0 border-t border-[var(--border)] space-y-3.5 text-xs sm:text-[13px] leading-relaxed"
                          >
                            <div className="space-y-1 pt-3">
                              <span className="font-semibold text-[var(--text)]">What we found: </span>
                              <span className="text-[var(--text-secondary)] max-w-[68ch]">
                                {whatFound}
                              </span>
                            </div>

                            {whyMatters && (
                              <div className="space-y-1">
                                <span className="font-semibold text-[var(--text)]">Why it matters: </span>
                                <span className="text-[var(--text-secondary)] max-w-[68ch]">
                                  {whyMatters}
                                </span>
                              </div>
                            )}

                            {howToFix && (
                              <div className="space-y-1">
                                <span className="font-semibold text-[var(--brand-400)]">How to fix it: </span>
                                <span className="text-[var(--text)] max-w-[68ch]">
                                  {howToFix}
                                </span>
                              </div>
                            )}

                            {example && (
                              <div className="mt-3 p-3.5 rounded-[9px] bg-[var(--bg-page)] border border-[var(--border)] space-y-2">
                                <div className="flex flex-col sm:flex-row sm:items-baseline gap-1 sm:gap-2">
                                  <span className="font-semibold text-[var(--risk-high)] shrink-0 sm:w-16">
                                    Before:
                                  </span>
                                  <span className="text-[var(--text-secondary)]">
                                    {example.before}
                                  </span>
                                </div>
                                <div className="flex flex-col sm:flex-row sm:items-baseline gap-1 sm:gap-2">
                                  <span className="font-semibold text-[var(--success)] shrink-0 sm:w-16">
                                    After:
                                  </span>
                                  <span className="text-[var(--text)]">
                                    {example.after}
                                  </span>
                                </div>
                              </div>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </section>
  );
}

