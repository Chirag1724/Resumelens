import { useState } from 'react';
import { CATEGORIES, TIER_RANGES, DISCLAIMER, GLOSSARY } from '../content/reportCopy';
import { pointsEarned } from '../lib/reportModel';
import { ChevronDown, Calculator, AlertTriangle, BookOpen } from 'lucide-react';

export default function DetailsSection({ data }) {
  const [openAccordions, setOpenAccordions] = useState({
    calc: false,
    limitations: false,
    terms: false,
  });

  const toggleAccordion = (key) => {
    setOpenAccordions((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  const rawSections = Array.isArray(data?.sectionScores) ? data.sectionScores : [];
  const categoryKeys = Object.keys(CATEGORIES);

  // Check if limit rule is relevant (either score capped or skills < 60)
  const skillsSection = rawSections.find((s) => s.sectionName === 'SKILLS_VALIDATION');
  const skillsScore = skillsSection ? Number(skillsSection.score) || 0 : 0;
  const isLimitRelevant = Boolean(data?.scoreCapped) || skillsScore < 60;

  return (
    <section id="details" aria-labelledby="details-heading" className="scroll-mt-36">
      {/* Section Header */}
      <div className="mb-5 space-y-1.5">
        <span className="text-[13px] font-semibold text-[var(--brand-500)] tracking-wide">
          Details
        </span>
        <h2
          id="details-heading"
          className="text-2xl sm:text-[28px] font-heading font-bold text-[var(--text)] leading-tight"
        >
          Details
        </h2>
        <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6] max-w-[60ch]">
          How this score is calculated, what this report cannot tell you, and terms used.
        </p>
      </div>

      <div className="space-y-4">
        {/* Accordion 1: How this score is calculated */}
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] overflow-hidden">
          <button
            type="button"
            onClick={() => toggleAccordion('calc')}
            aria-expanded={openAccordions.calc}
            aria-controls="panel-calc"
            id="header-calc"
            className="w-full p-4 sm:p-5 flex items-center justify-between gap-3 text-left hover:bg-[var(--bg-elevated)] transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
          >
            <div className="flex items-center gap-2.5">
              <Calculator className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
              <h3 className="text-sm sm:text-base font-heading font-bold text-[var(--text)]">
                How this score is calculated
              </h3>
            </div>
            <ChevronDown
              className={`w-4 h-4 text-[var(--text-tertiary)] shrink-0 transition-transform duration-200 ${
                openAccordions.calc ? 'rotate-180' : ''
              }`}
              aria-hidden="true"
            />
          </button>

          {openAccordions.calc && (
            <div
              id="panel-calc"
              role="region"
              aria-labelledby="header-calc"
              className="p-4 sm:p-5 pt-0 border-t border-[var(--border)] space-y-4 text-xs sm:text-[13px] leading-relaxed"
            >
              <div className="overflow-x-auto pt-3">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-[var(--border)] text-[var(--text-tertiary)] text-[11px] font-medium">
                      <th className="py-2 pr-4">Category</th>
                      <th className="py-2 px-4">Weight</th>
                      <th className="py-2 px-4">Score</th>
                      <th className="py-2 pl-4 text-right">Points earned</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[var(--border)]">
                    {categoryKeys.map((catKey) => {
                      const cat = CATEGORIES[catKey];
                      const s = rawSections.find((item) => item.sectionName === catKey);
                      const score = s ? Math.max(0, Math.min(100, Math.round(Number(s.score) || 0))) : 0;
                      const earned = pointsEarned(score, cat.weight);
                      return (
                        <tr key={catKey} className="text-[var(--text-secondary)]">
                          <td className="py-2.5 pr-4 font-medium text-[var(--text)]">
                            {cat.label}
                          </td>
                          <td className="py-2.5 px-4">{cat.weight}%</td>
                          <td className="py-2.5 px-4">{score} / 100</td>
                          <td className="py-2.5 pl-4 text-right font-mono text-[var(--text)]">
                            {earned}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>

              <div className="p-3 rounded-[8px] bg-[var(--bg-page)] border border-[var(--border)] space-y-2">
                <p className="font-semibold text-[var(--text)]">
                  Overall score = sum of points earned
                </p>
                <div className="text-[var(--text-secondary)] space-y-1">
                  <span className="font-medium text-[var(--text)]">Tier ranges:</span>
                  <ul className="grid grid-cols-2 sm:grid-cols-3 gap-1 pt-1 text-xs">
                    <li><strong className="text-[var(--text)]">Excellent:</strong> {TIER_RANGES.EXCELLENT}</li>
                    <li><strong className="text-[var(--text)]">Strong:</strong> {TIER_RANGES.STRONG}</li>
                    <li><strong className="text-[var(--text)]">Moderate:</strong> {TIER_RANGES.MODERATE}</li>
                    <li><strong className="text-[var(--text)]">Weak:</strong> {TIER_RANGES.WEAK}</li>
                    <li><strong className="text-[var(--text)]">Poor:</strong> {TIER_RANGES.POOR}</li>
                  </ul>
                </div>
              </div>

              {isLimitRelevant && (
                <div className="p-3 rounded-[8px] bg-[var(--accent-orange)]/10 border border-[var(--accent-orange)]/25 text-xs text-[var(--text)]">
                  <p>
                    If the skills match is below 40, the overall score is limited to 54. From 40 to 59 it is limited to 69.
                  </p>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Accordion 2: What this report cannot tell you */}
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] overflow-hidden">
          <button
            type="button"
            onClick={() => toggleAccordion('limitations')}
            aria-expanded={openAccordions.limitations}
            aria-controls="panel-limitations"
            id="header-limitations"
            className="w-full p-4 sm:p-5 flex items-center justify-between gap-3 text-left hover:bg-[var(--bg-elevated)] transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
          >
            <div className="flex items-center gap-2.5">
              <AlertTriangle className="w-4 h-4 text-[var(--warning)]" aria-hidden="true" />
              <h3 className="text-sm sm:text-base font-heading font-bold text-[var(--text)]">
                What this report cannot tell you
              </h3>
            </div>
            <ChevronDown
              className={`w-4 h-4 text-[var(--text-tertiary)] shrink-0 transition-transform duration-200 ${
                openAccordions.limitations ? 'rotate-180' : ''
              }`}
              aria-hidden="true"
            />
          </button>

          {openAccordions.limitations && (
            <div
              id="panel-limitations"
              role="region"
              aria-labelledby="header-limitations"
              className="p-4 sm:p-5 pt-0 border-t border-[var(--border)] space-y-3 text-xs sm:text-[13px] leading-relaxed"
            >
              <p className="pt-3 text-[var(--text-secondary)] italic">
                {DISCLAIMER}
              </p>
              <ul className="space-y-1.5 text-[var(--text)] list-disc list-inside">
                <li>It compares keywords and wording. It does not understand meaning.</li>
                <li>It works best for English resumes.</li>
                <li>It does not check whether your experience is true.</li>
              </ul>
            </div>
          )}
        </div>

        {/* Accordion 3: Terms used */}
        <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] overflow-hidden">
          <button
            type="button"
            onClick={() => toggleAccordion('terms')}
            aria-expanded={openAccordions.terms}
            aria-controls="panel-terms"
            id="header-terms"
            className="w-full p-4 sm:p-5 flex items-center justify-between gap-3 text-left hover:bg-[var(--bg-elevated)] transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)]"
          >
            <div className="flex items-center gap-2.5">
              <BookOpen className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
              <h3 className="text-sm sm:text-base font-heading font-bold text-[var(--text)]">
                Terms used
              </h3>
            </div>
            <ChevronDown
              className={`w-4 h-4 text-[var(--text-tertiary)] shrink-0 transition-transform duration-200 ${
                openAccordions.terms ? 'rotate-180' : ''
              }`}
              aria-hidden="true"
            />
          </button>

          {openAccordions.terms && (
            <div
              id="panel-terms"
              role="region"
              aria-labelledby="header-terms"
              className="p-4 sm:p-5 pt-0 border-t border-[var(--border)] space-y-3 text-xs sm:text-[13px] leading-relaxed"
            >
              <dl className="pt-3 space-y-3">
                <div>
                  <dt className="font-bold text-[var(--text)]">ATS</dt>
                  <dd className="text-[var(--text-secondary)]">{GLOSSARY.ATS}</dd>
                </div>
                <div>
                  <dt className="font-bold text-[var(--text)]">Keyword</dt>
                  <dd className="text-[var(--text-secondary)]">{GLOSSARY.keyword}</dd>
                </div>
                <div>
                  <dt className="font-bold text-[var(--text)]">Tier</dt>
                  <dd className="text-[var(--text-secondary)]">{GLOSSARY.tier}</dd>
                </div>
              </dl>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

