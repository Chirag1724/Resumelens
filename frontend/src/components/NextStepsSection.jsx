import { Link } from 'react-router-dom';
import { Sparkles, RotateCcw } from 'lucide-react';
import { DISCLAIMER } from '../content/reportCopy';

export default function NextStepsSection({ onImproveClick }) {
  return (
    <section id="next-steps" aria-labelledby="next-steps-heading" className="space-y-8 scroll-mt-36">
      {/* Section Header */}
      <div className="mb-5 space-y-1.5">
        <span className="text-[13px] font-semibold text-[var(--brand-500)] tracking-wide">
          Next steps
        </span>
        <h2
          id="next-steps-heading"
          className="text-2xl sm:text-[28px] font-heading font-bold text-[var(--text)] leading-tight"
        >
          Take action on your report
        </h2>
        <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6] max-w-[60ch]">
          Apply suggestions with our prompt assistant or scan a refreshed resume.
        </p>
      </div>

      {/* Two cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
        {/* Card 1: Improve your wording */}
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 sm:p-6 flex flex-col justify-between gap-5">
          <div className="space-y-4">
            <h3 className="text-lg sm:text-xl font-heading font-bold text-[var(--text)]">
              Improve your wording
            </h3>
            <ol className="space-y-2.5 text-xs sm:text-[13px] text-[var(--text-secondary)] leading-relaxed">
              <li className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-[var(--brand-500)]/15 text-[var(--brand-500)] font-bold text-xs flex items-center justify-center shrink-0 mt-0.5">
                  1
                </span>
                <span>Generate the improvement prompt</span>
              </li>
              <li className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-[var(--brand-500)]/15 text-[var(--brand-500)] font-bold text-xs flex items-center justify-center shrink-0 mt-0.5">
                  2
                </span>
                <span>Paste it into an AI assistant</span>
              </li>
              <li className="flex items-start gap-2.5">
                <span className="w-5 h-5 rounded-full bg-[var(--brand-500)]/15 text-[var(--brand-500)] font-bold text-xs flex items-center justify-center shrink-0 mt-0.5">
                  3
                </span>
                <span>Open the result in a LaTeX editor such as Overleaf</span>
              </li>
            </ol>
          </div>

          <button
            type="button"
            onClick={onImproveClick}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] active:scale-[0.98] text-[var(--on-brand)] text-sm font-semibold transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
          >
            <Sparkles className="w-4 h-4" aria-hidden="true" />
            <span>Generate improvement prompt</span>
          </button>
        </div>

        {/* Card 2: Updated your resume? */}
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 sm:p-6 flex flex-col justify-between gap-5">
          <div className="space-y-3">
            <h3 className="text-lg sm:text-xl font-heading font-bold text-[var(--text)]">
              Updated your resume?
            </h3>
            <p className="text-xs sm:text-[13px] text-[var(--text-secondary)] leading-relaxed max-w-[68ch]">
              Once you have updated your resume with the missing skills and fixes, run another scan to see how your score improves.
            </p>
          </div>

          <Link
            to="/"
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-5 py-2.5 rounded-[9px] bg-[var(--bg-elevated)] hover:bg-[var(--bg-page)] active:scale-[0.98] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-sm font-semibold transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
          >
            <RotateCcw className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
            <span>Re-analyze</span>
          </Link>
        </div>
      </div>

      {/* Footer line with the DISCLAIMER */}
      <footer className="pt-6 border-t border-[var(--border)]">
        <p className="text-xs text-[var(--text-tertiary)] text-center leading-relaxed max-w-2xl mx-auto">
          {DISCLAIMER}
        </p>
      </footer>
    </section>
  );
}

