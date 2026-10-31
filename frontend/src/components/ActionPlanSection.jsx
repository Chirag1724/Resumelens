import { useState } from 'react';
import { buildActionPlan } from '../lib/reportModel';
import {
  Check,
  ChevronDown,
  Sparkles,
  Clock,
  AlertCircle,
  AlertTriangle,
  Info,
  CheckCircle2,
  Quote,
} from 'lucide-react';

export default function ActionPlanSection({ data }) {
  const plan = buildActionPlan(data);
  const [doneMap, setDoneMap] = useState({});
  const [expandedExamples, setExpandedExamples] = useState({});

  const toggleDone = (id) => {
    setDoneMap((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  const toggleExample = (id) => {
    setExpandedExamples((prev) => ({
      ...prev,
      [id]: !prev[id],
    }));
  };

  const getImportanceIcon = (importance) => {
    const text = String(importance || '').toLowerCase();
    if (text.includes('high')) return AlertCircle;
    if (text.includes('medium')) return AlertTriangle;
    return Info;
  };

  const getImportanceStyle = (importance) => {
    const text = String(importance || '').toLowerCase();
    if (text.includes('high')) {
      return {
        color: 'var(--risk-high)',
        backgroundColor: 'rgba(239, 68, 68, 0.12)',
        borderColor: 'rgba(239, 68, 68, 0.28)',
      };
    }
    if (text.includes('medium')) {
      return {
        color: 'var(--risk-medium)',
        backgroundColor: 'rgba(251, 201, 4, 0.12)',
        borderColor: 'rgba(251, 201, 4, 0.28)',
      };
    }
    return {
      color: 'var(--risk-low)',
      backgroundColor: 'rgba(153, 153, 153, 0.12)',
      borderColor: 'rgba(153, 153, 153, 0.28)',
    };
  };

  return (
    <section id="plan" aria-labelledby="plan-heading" className="scroll-mt-36">
      {/* Section Header */}
      <div className="mb-5 space-y-1.5">
        <span className="text-[13px] font-semibold text-[var(--brand-500)] tracking-wide">
          Action plan
        </span>
        <h2
          id="plan-heading"
          className="text-2xl sm:text-[28px] font-heading font-bold text-[var(--text)] leading-tight"
        >
          Action plan
        </h2>
        <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6] max-w-[60ch]">
          Do these in order. The first ones help the most.
        </p>
      </div>

      {/* Empty State */}
      {plan.length === 0 ? (
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 sm:p-6 text-center">
          <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6]">
            No action needed. Your resume matches this job well.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {plan.map((item, index) => {
            const isDone = Boolean(doneMap[item.id]);
            const isExampleOpen = Boolean(expandedExamples[item.id]);
            const ImportanceIcon = getImportanceIcon(item.importance);
            const importanceStyle = getImportanceStyle(item.importance);

            return (
              <article
                key={item.id || index}
                className={`bg-[var(--bg-card)] border rounded-[12px] p-5 sm:p-6 transition-all ${
                  isDone
                    ? 'border-[var(--border)] opacity-80 bg-[var(--bg-card)]/60'
                    : 'border-[var(--border)] hover:border-[var(--border-strong)]'
                }`}
              >
                <div className="space-y-4">
                  {/* Top Row: Number, Title, Chips, Done Checkbox */}
                  <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
                    <div className="flex items-start gap-3 min-w-0">
                      <span
                        className={`w-7 h-7 rounded-full text-xs font-bold flex items-center justify-center shrink-0 mt-0.5 ${
                          isDone
                            ? 'bg-[var(--success)]/20 text-[var(--success)]'
                            : 'bg-[var(--brand-500)]/20 text-[var(--brand-500)]'
                        }`}
                        aria-hidden="true"
                      >
                        {isDone ? <Check className="w-4 h-4" /> : index + 1}
                      </span>
                      <div className="space-y-2 min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <h3
                            className={`text-base sm:text-lg font-heading font-bold text-[var(--text)] ${
                              isDone ? 'line-through opacity-75' : ''
                            }`}
                          >
                            {item.title}
                          </h3>
                          {isDone && (
                            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-[4px] bg-[var(--success)]/15 border border-[var(--success)]/30 text-[11px] font-semibold text-[var(--success)]">
                              <CheckCircle2 className="w-3 h-3" aria-hidden="true" />
                              <span>Completed</span>
                            </span>
                          )}
                        </div>

                        {/* Chips Row */}
                        <div className="flex flex-wrap items-center gap-2 text-xs">
                          {item.importance && (
                            <span
                              className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-[6px] border font-medium text-[11px]"
                              style={importanceStyle}
                            >
                              <ImportanceIcon className="w-3 h-3 shrink-0" aria-hidden="true" />
                              <span>{item.importance}</span>
                            </span>
                          )}

                          {item.effort && (
                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-[6px] bg-[var(--bg-page)] border border-[var(--border)] text-[var(--text-secondary)] text-[11px]">
                              <Clock className="w-3 h-3 text-[var(--text-tertiary)]" aria-hidden="true" />
                              <span>{item.effort}</span>
                            </span>
                          )}

                          {item.gainText && (
                            <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-[6px] bg-[var(--brand-500)]/10 border border-[var(--brand-500)]/25 text-[var(--brand-400)] text-[11px]">
                              <Sparkles className="w-3 h-3" aria-hidden="true" />
                              <span>{item.gainText}</span>
                            </span>
                          )}
                        </div>
                      </div>
                    </div>

                    {/* Mark as done checkbox */}
                    <label className="inline-flex items-center gap-2 text-xs text-[var(--text-secondary)] hover:text-[var(--text)] cursor-pointer select-none shrink-0 self-start sm:self-center bg-[var(--bg-page)] border border-[var(--border)] px-3 py-1.5 rounded-[8px] transition-colors focus-within:ring-2 focus-within:ring-[var(--brand-500)]">
                      <input
                        type="checkbox"
                        checked={isDone}
                        onChange={() => toggleDone(item.id)}
                        className="w-4 h-4 rounded border-[var(--border)] text-[var(--brand-500)] focus:ring-[var(--brand-500)] accent-[var(--brand-500)] cursor-pointer"
                      />
                      <span className="font-medium">Mark as done</span>
                    </label>
                  </div>

                  {/* Why and How */}
                  <div className="space-y-2 pt-1 border-t border-[var(--border)]/60 text-[15px] leading-[1.6]">
                    <div>
                      <span className="font-semibold text-[var(--text)]">Why it matters: </span>
                      <span className="text-[var(--text-secondary)] max-w-[68ch]">
                        {item.why}
                      </span>
                    </div>
                    <div>
                      <span className="font-semibold text-[var(--text)]">How to do it: </span>
                      <span className="text-[var(--text)] max-w-[68ch]">
                        {item.how}
                      </span>
                    </div>
                  </div>

                  {/* From your resume quote block */}
                  {item.fromResume && (
                    <div className="pt-1">
                      <div className="p-3.5 rounded-[9px] bg-[var(--bg-page)] border-l-2 border-l-[var(--brand-500)] border-y border-r border-[var(--border)] text-xs sm:text-sm text-[var(--text-secondary)] space-y-1">
                        <div className="flex items-center gap-1.5 text-[11px] font-semibold text-[var(--brand-400)]">
                          <Quote className="w-3 h-3" aria-hidden="true" />
                          <span>From your resume</span>
                        </div>
                        <blockquote className="italic leading-relaxed text-[var(--text)]">
                          "{item.fromResume}"
                        </blockquote>
                      </div>
                    </div>
                  )}

                  {/* Show example disclosure */}
                  {item.example && (
                    <div className="pt-1">
                      <button
                        type="button"
                        onClick={() => toggleExample(item.id)}
                        aria-expanded={isExampleOpen}
                        className="inline-flex items-center gap-1.5 text-xs font-semibold text-[var(--brand-500)] hover:text-[var(--brand-400)] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)] rounded-[4px] cursor-pointer"
                      >
                        <span>{isExampleOpen ? 'Hide example' : 'Show example'}</span>
                        <ChevronDown
                          className={`w-3.5 h-3.5 transition-transform duration-200 ${
                            isExampleOpen ? 'rotate-180' : ''
                          }`}
                          aria-hidden="true"
                        />
                      </button>

                      {isExampleOpen && (
                        <div className="mt-2.5 p-3.5 rounded-[9px] bg-[var(--bg-page)] border border-[var(--border)] space-y-2 text-xs sm:text-sm leading-relaxed">
                          <div className="flex flex-col sm:flex-row sm:items-baseline gap-1 sm:gap-2">
                            <span className="font-semibold text-[var(--risk-high)] shrink-0 sm:w-16">
                              Before:
                            </span>
                            <span className="text-[var(--text-secondary)]">
                              {item.example.before}
                            </span>
                          </div>
                          <div className="flex flex-col sm:flex-row sm:items-baseline gap-1 sm:gap-2">
                            <span className="font-semibold text-[var(--success)] shrink-0 sm:w-16">
                              After:
                            </span>
                            <span className="text-[var(--text)]">
                              {item.example.after}
                            </span>
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

