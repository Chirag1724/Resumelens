import ScoreGauge from './ScoreGauge';
import {
  tierFor,
  verdictFor,
  capInfo,
  summaryFacts,
  buildActionPlan,
  scaleMarkerPosition,
} from '../lib/reportModel';
import { formatScore } from '../lib/format';
import { TIER_RANGES } from '../content/reportCopy';
import {
  CheckCircle2,
  AlertTriangle,
  TrendingUp,
  SlidersHorizontal,
  ArrowRight,
  Clock,
  Sparkles,
  Info,
  AlertCircle,
  ShieldAlert,
} from 'lucide-react';

const SCALE_TIERS = [
  { key: 'POOR', label: 'Poor', token: 'var(--score-poor)', range: TIER_RANGES.POOR, icon: AlertCircle, width: 40 },
  { key: 'WEAK', label: 'Weak', token: 'var(--score-weak)', range: TIER_RANGES.WEAK, icon: AlertTriangle, width: 15 },
  { key: 'MODERATE', label: 'Moderate', token: 'var(--score-moderate)', range: TIER_RANGES.MODERATE, icon: Info, width: 15 },
  { key: 'STRONG', label: 'Strong', token: 'var(--score-strong)', range: TIER_RANGES.STRONG, icon: CheckCircle2, width: 15 },
  { key: 'EXCELLENT', label: 'Excellent', token: 'var(--score-excellent)', range: TIER_RANGES.EXCELLENT, icon: Sparkles, width: 15 },
];

export default function OverviewSection({ data }) {
  const rawOverallScore = Number(data?.overallScore) || 0;
  const currentTier = tierFor(rawOverallScore);
  const verdict = verdictFor(rawOverallScore, data);
  const cap = capInfo(data);
  const facts = summaryFacts(data);
  const isKeywordListEmpty = facts.skillsCovered === 'Not scored';
  const plan = buildActionPlan(data);
  const topPlanItems = plan.slice(0, 3);
  const markerPos = scaleMarkerPosition(rawOverallScore);

  return (
    <section id="overview" aria-labelledby="overview-heading" className="scroll-mt-36">
      {/* Section Header */}
      <div className="mb-5 space-y-1.5">
        <h2
          id="overview-heading"
          className="text-2xl sm:text-[28px] font-heading font-bold text-[var(--text)] leading-tight [text-wrap:balance]"
        >
          Overview
        </h2>
        <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6] max-w-[60ch] [text-wrap:balance]">
          Your match and the most important things to do.
        </p>
      </div>

      {/* Two-column Hero Grid aligned to top */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Column (5 cols): Gauge Card with Scale Bar and Notice */}
        <div className="lg:col-span-5 bg-[var(--bg-card)] border border-[var(--border)] rounded-[18px] p-5 sm:p-6 flex flex-col gap-5">
          <div className="flex flex-col items-center">
            <h3 className="sr-only">Overall match score</h3>
            <ScoreGauge score={rawOverallScore} showLabel={true} scoreCapped={false} />
          </div>

          {/* Segmented scale bar showing the 5 tiers with proportional widths */}
          <div className="w-full space-y-3 pt-2">
            <div className="relative pt-6">
              {/* Marker at linear score position */}
              <div
                className="absolute top-0 -translate-x-1/2 flex flex-col items-center transition-all duration-300"
                style={{ left: `${markerPos}%` }}
                aria-hidden="true"
              >
                <span className="px-2 py-0.5 rounded-[4px] bg-[var(--text)] text-[var(--bg-page)] text-[11px] font-bold shadow-sm whitespace-nowrap">
                  {formatScore(rawOverallScore)}
                </span>
                <span className="w-0 h-0 border-x-4 border-x-transparent border-t-4 border-t-[var(--text)] mt-0.5" />
              </div>

              {/* Segmented proportional bar */}
              <div
                role="meter"
                aria-label="Score tier scale"
                aria-valuenow={rawOverallScore}
                aria-valuemin="0"
                aria-valuemax="100"
                aria-valuetext={`${formatScore(rawOverallScore)} out of 100, rated ${currentTier}`}
                className="flex gap-1.5 h-2.5 w-full rounded-full overflow-hidden bg-white/5 p-0.5"
              >
                {SCALE_TIERS.map((tier) => (
                  <div
                    key={tier.key}
                    className="h-full rounded-full transition-opacity"
                    style={{
                      width: `${tier.width}%`,
                      backgroundColor: tier.token,
                      opacity: tier.key === currentTier ? 1 : 0.45,
                    }}
                  />
                ))}
              </div>
            </div>

            {/* Scale range labels */}
            <div className="flex gap-1 text-center w-full">
              {SCALE_TIERS.map((tier) => {
                const isSelected = tier.key === currentTier;
                const IconComponent = tier.icon;
                return (
                  <div
                    key={tier.key}
                    className={`flex flex-col items-center gap-0.5 text-[11px] transition-colors ${
                      isSelected ? 'text-[var(--text)] font-semibold' : 'text-[var(--text-tertiary)]'
                    }`}
                    style={{ width: `${tier.width}%` }}
                  >
                    <span className="inline-flex items-center gap-1">
                      <IconComponent className="w-3 h-3 shrink-0" aria-hidden="true" />
                      <span className="hidden sm:inline">{tier.label}</span>
                    </span>
                    <span className="text-[10px] text-[var(--text-tertiary)] leading-tight">
                      {tier.range}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Highlighted gauge notice when capped */}
          {!isKeywordListEmpty && cap && (
            <div className="p-4 rounded-[12px] bg-[var(--accent-orange)]/10 border border-[var(--accent-orange)]/30 text-xs sm:text-[13px] leading-relaxed text-[var(--text)] space-y-1 mt-2">
              <div className="flex items-start gap-2">
                <ShieldAlert className="w-4 h-4 text-[var(--accent-orange)] shrink-0 mt-0.5" aria-hidden="true" />
                <div className="space-y-1">
                  <p>
                    Your score is held at {formatScore(cap.cap)} because your skills match ({formatScore(cap.skillsNow)}) is below {formatScore(cap.threshold)}. Your resume on its own would score {formatScore(cap.uncapped)}.
                  </p>
                  <p className="text-[var(--text-secondary)]">
                    Raise your skills match to {formatScore(cap.threshold)} to lift this limit.{cap.moreSkills !== null ? ` That is about ${cap.moreSkills} more of the job's skills.` : ''}
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right Column (7 cols): Verdict Card + 2x2 Fact Cards Grid */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          {/* Verdict Card: Headline, Body, and Start here list directly under the text */}
          <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[18px] p-5 sm:p-6 flex flex-col gap-5">
            <div className="space-y-2">
              <h3 className="text-xl sm:text-2xl font-heading font-bold text-[var(--text)] leading-snug [text-wrap:balance]">
                {verdict.headline}
              </h3>
              <p className="text-[15px] leading-[1.6] text-[var(--text-secondary)] max-w-[68ch] [text-wrap:balance]">
                {verdict.body}
              </p>
              {(isKeywordListEmpty || verdict.note) && (
                <p className="text-xs sm:text-[13px] leading-relaxed text-[var(--text-secondary)] max-w-[68ch]">
                  {verdict.note || "This score reflects your resume's quality only. We could not find specific skills in the job description, so the job match is not included."}
                </p>
              )}
            </div>

            {/* Start here section with top 3 plan items directly under text */}
            <div className="space-y-3 pt-4 border-t border-[var(--border)]">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold text-[var(--brand-500)]">
                  Start here
                </span>
                <a
                  href="#plan"
                  className="inline-flex items-center gap-1 text-xs font-semibold text-[var(--brand-500)] hover:text-[var(--brand-400)] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)] rounded-[4px]"
                >
                  <span>See all steps</span>
                  <ArrowRight className="w-3.5 h-3.5" aria-hidden="true" />
                </a>
              </div>

              {topPlanItems.length > 0 ? (
                <ol className="space-y-2.5">
                  {topPlanItems.map((item, index) => (
                    <li
                      key={item.id || index}
                      className="flex items-center justify-between gap-3 p-3 rounded-[9px] bg-[var(--bg-page)] border border-[var(--border)]"
                    >
                      <div className="flex items-center gap-2.5 min-w-0">
                        <span className="w-6 h-6 rounded-full bg-[var(--brand-500)]/15 text-[var(--brand-500)] font-bold text-xs flex items-center justify-center shrink-0">
                          {index + 1}
                        </span>
                        <span className="text-xs sm:text-sm font-semibold text-[var(--text)] truncate">
                          {item.title}
                        </span>
                      </div>
                      {item.effort && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] text-[11px] text-[var(--text-secondary)] shrink-0">
                          <Clock className="w-3 h-3 text-[var(--text-tertiary)]" aria-hidden="true" />
                          <span>{item.effort}</span>
                        </span>
                      )}
                    </li>
                  ))}
                </ol>
              ) : (
                <p className="text-xs text-[var(--text-secondary)]">
                  No immediate action required.
                </p>
              )}
            </div>
          </div>

          {/* Four fact cards arranged as a 2 by 2 grid directly under the verdict card */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Fact 1: Skills covered */}
            <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col justify-between gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs text-[var(--text-tertiary)] font-medium">Skills covered</span>
                <CheckCircle2 className="w-4 h-4 text-[var(--success)]" aria-hidden="true" />
              </div>
              <p className="text-lg sm:text-xl font-heading font-bold text-[var(--text)]">
                {facts.skillsCovered}
              </p>
              {facts.percent !== null && facts.percent !== undefined ? (
                <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
                  {facts.percent}% of the skills asked for
                </p>
              ) : (
                <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
                  Job match is not scored
                </p>
              )}
            </div>

            {/* Fact 2: Action steps */}
            <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col justify-between gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs text-[var(--text-tertiary)] font-medium">Action steps</span>
                <AlertTriangle className="w-4 h-4 text-[var(--warning)]" aria-hidden="true" />
              </div>
              <p className="text-lg sm:text-xl font-heading font-bold text-[var(--text)]">
                {plan.length === 0 ? 'Nothing to do' : `${plan.length} steps`}
              </p>
              {plan.length > 0 && (
                <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
                  Start with step 1.
                </p>
              )}
            </div>

            {/* Fact 3: Strongest area */}
            <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col justify-between gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs text-[var(--text-tertiary)] font-medium">Strongest area</span>
                <TrendingUp className="w-4 h-4 text-[var(--score-strong)]" aria-hidden="true" />
              </div>
              <p className="text-lg sm:text-xl font-heading font-bold text-[var(--text)] truncate">
                {facts.strongestCategory}
              </p>
              <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
                {formatScore(facts.strongestScore)} out of 100
              </p>
            </div>

            {/* Fact 4: Needs most work */}
            <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 flex flex-col justify-between gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs text-[var(--text-tertiary)] font-medium">Needs most work</span>
                <SlidersHorizontal className="w-4 h-4 text-[var(--accent-orange)]" aria-hidden="true" />
              </div>
              <p className="text-lg sm:text-xl font-heading font-bold text-[var(--text)] truncate">
                {facts.weakestCategory}
              </p>
              <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
                {formatScore(facts.weakestScore)} out of 100
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
