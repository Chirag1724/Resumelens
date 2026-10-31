import { CATEGORIES } from '../content/reportCopy';
import { tierFor, pointsEarned } from '../lib/reportModel';
import { getScoreDetails } from '../lib/score';
import {
  FileCheck,
  Cpu,
  TrendingUp,
  Layers,
  PenTool,
  ShieldCheck,
  CheckCircle2,
  Sparkles,
  Info,
  AlertTriangle,
  AlertCircle,
  Check,
} from 'lucide-react';

const CATEGORY_ICONS = {
  ATS_COMPATIBILITY: FileCheck,
  SKILLS_VALIDATION: Cpu,
  IMPACT_ACHIEVEMENTS: TrendingUp,
  PROJECT_QUALITY: Layers,
  WRITING_QUALITY: PenTool,
  PROFESSIONALISM: ShieldCheck,
};

const TIER_ICONS = {
  EXCELLENT: Sparkles,
  STRONG: CheckCircle2,
  MODERATE: Info,
  WEAK: AlertTriangle,
  POOR: AlertCircle,
};

export default function ScoreBreakdownSection({ data }) {
  const rawSections = Array.isArray(data?.sectionScores) ? data.sectionScores : [];
  const categoryKeys = Object.keys(CATEGORIES);

  return (
    <section id="breakdown" aria-labelledby="breakdown-heading" className="scroll-mt-36">
      {/* Section Header */}
      <div className="mb-5 space-y-1.5">
        <span className="text-[13px] font-semibold text-[var(--brand-500)] tracking-wide">
          Score breakdown
        </span>
        <h2
          id="breakdown-heading"
          className="text-2xl sm:text-[28px] font-heading font-bold text-[var(--text)] leading-tight"
        >
          Score breakdown
        </h2>
        <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6] max-w-[60ch]">
          A detailed look at your scores across the six evaluation categories.
        </p>
      </div>

      {/* Six cards grid: 2 cols on tablet/desktop, 1 on mobile */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6">
        {categoryKeys.map((catKey) => {
          const cat = CATEGORIES[catKey];
          const sectionData = rawSections.find((s) => s.sectionName === catKey);
          const score = sectionData ? Math.max(0, Math.min(100, Math.round(Number(sectionData.score) || 0))) : 0;
          const tier = tierFor(score);
          const tierInfo = getScoreDetails(score);
          const TierIcon = TIER_ICONS[tier] || Info;
          const CategoryIcon = CATEGORY_ICONS[catKey] || FileCheck;
          const earned = pointsEarned(score, cat.weight);

          const meaningLine =
            tier === 'EXCELLENT' || tier === 'STRONG'
              ? cat.good
              : tier === 'MODERATE'
              ? cat.partial
              : cat.needs;

          return (
            <article
              key={catKey}
              className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 sm:p-6 flex flex-col justify-between gap-4"
            >
              <div className="space-y-4">
                {/* Header: Icon, Label, Weight Chip */}
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-2.5">
                    <div className="w-9 h-9 rounded-[8px] bg-[var(--brand-500)]/15 border border-[var(--brand-500)]/25 flex items-center justify-center shrink-0">
                      <CategoryIcon className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
                    </div>
                    <h3 className="text-base sm:text-lg font-heading font-bold text-[var(--text)]">
                      {cat.label}
                    </h3>
                  </div>
                  <span className="px-2.5 py-1 rounded-[6px] bg-[var(--bg-page)] border border-[var(--border)] text-xs text-[var(--text-secondary)] font-medium shrink-0">
                    {cat.weight} percent of your score
                  </span>
                </div>

                {/* Score & Progress Bar */}
                <div className="space-y-2">
                  <div className="flex items-baseline justify-between gap-2">
                    <div className="flex items-baseline gap-2">
                      <span className="text-2xl sm:text-3xl font-heading font-extrabold text-[var(--text)]">
                        {score}
                      </span>
                      <span className="text-xs text-[var(--text-tertiary)]">/ 100</span>
                    </div>

                    <div className="flex items-center gap-2">
                      <span
                        className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-[6px] text-xs font-semibold"
                        style={{
                          backgroundColor: `${tierInfo.color}1F`,
                          color: tierInfo.color,
                          border: `1px solid ${tierInfo.color}40`,
                        }}
                      >
                        <TierIcon className="w-3 h-3" aria-hidden="true" />
                        <span>{tierInfo.label}</span>
                      </span>
                      <span className="text-xs text-[var(--text-secondary)] font-mono">
                        Points earned: {earned}
                      </span>
                    </div>
                  </div>

                  <div
                    role="progressbar"
                    aria-label={`${cat.label} score`}
                    aria-valuenow={score}
                    aria-valuemin="0"
                    aria-valuemax="100"
                    className="w-full h-2 rounded-full bg-[var(--bg-page)] border border-[var(--border)] overflow-hidden"
                  >
                    <div
                      className="h-full rounded-full transition-all duration-500"
                      style={{
                        width: `${score}%`,
                        backgroundColor: tierInfo.color,
                      }}
                    />
                  </div>
                </div>

                {/* What it is and Meaning line */}
                <div className="space-y-1.5 text-xs sm:text-[13px] leading-relaxed pt-1">
                  <p className="text-[var(--text-secondary)] max-w-[68ch]">
                    {cat.what}
                  </p>
                  <p className="font-medium text-[var(--text)] max-w-[68ch]">
                    {meaningLine}
                  </p>
                </div>
              </div>

              {/* How to improve tips or Nothing to fix */}
              <div className="pt-3 border-t border-[var(--border)] text-xs sm:text-[13px]">
                {score < 85 ? (
                  <div className="space-y-1.5">
                    <span className="font-semibold text-[var(--brand-400)] text-xs">
                      How to improve:
                    </span>
                    <ul className="space-y-1 text-[var(--text-secondary)] list-disc list-inside leading-relaxed">
                      {cat.tips.map((tip, idx) => (
                        <li key={idx} className="max-w-[68ch]">{tip}</li>
                      ))}
                    </ul>
                  </div>
                ) : (
                  <p className="text-[var(--success)] flex items-center gap-1.5 font-medium">
                    <Check className="w-3.5 h-3.5" aria-hidden="true" />
                    <span>Nothing to fix here.</span>
                  </p>
                )}
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}

