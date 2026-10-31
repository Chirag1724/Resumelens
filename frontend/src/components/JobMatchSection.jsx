import { Check, Plus, Lightbulb, CheckCircle2 } from 'lucide-react';
import { keywordBand } from '../lib/reportModel';

function parseKeywords(raw) {
  if (!raw) return [];
  if (Array.isArray(raw)) return raw;
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) return parsed;
    } catch {
      return raw.split(',').map((s) => s.trim()).filter(Boolean);
    }
  }
  return [];
}

export default function JobMatchSection({ data }) {
  const sections = Array.isArray(data?.sectionScores) ? data.sectionScores : [];
  const skillsSection = sections.find((s) => s.sectionName === 'SKILLS_VALIDATION');
  const matched = parseKeywords(skillsSection?.matchedKeywords);
  const missing = parseKeywords(skillsSection?.missingKeywords);
  const totalKeywords = matched.length + missing.length;
  const percent = totalKeywords > 0
    ? Math.round((matched.length / totalKeywords) * 100)
    : Math.round(Number(data?.keywordMatchPercent) || 0);

  const band = keywordBand(percent);
  let bandSentence = 'Low coverage. Many of the job\'s skills are missing.';
  if (band === 'great') {
    bandSentence = 'Great coverage.';
  } else if (band === 'decent') {
    bandSentence = 'Decent coverage. Add the missing skills you have.';
  }

  const missingPercent = Math.max(0, 100 - percent);

  return (
    <section id="job-match" aria-labelledby="job-match-heading" className="scroll-mt-36">
      {/* Section Header */}
      <div className="mb-5 space-y-1.5">
        <span className="text-[13px] font-semibold text-[var(--brand-500)] tracking-wide">
          Job match
        </span>
        <h2
          id="job-match-heading"
          className="text-2xl sm:text-[28px] font-heading font-bold text-[var(--text)] leading-tight"
        >
          Job match
        </h2>
        <p className="text-[15px] text-[var(--text-secondary)] leading-[1.6] max-w-[60ch]">
          Skills found on your resume compared to skills from the job description.
        </p>
      </div>

      <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-5 sm:p-6 space-y-6">
        {/* Coverage sentence and band */}
        <div className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-baseline justify-between gap-2">
            <p className="text-base sm:text-lg font-heading font-bold text-[var(--text)]">
              You cover {matched.length} of {totalKeywords} skills this job asks for ({percent}%)
            </p>
            <span className="text-xs sm:text-sm font-medium text-[var(--text-secondary)]">
              {bandSentence}
            </span>
          </div>

          {/* Two-part bar for matched and missing */}
          <div
            role="progressbar"
            aria-label="Skill match progress"
            aria-valuenow={percent}
            aria-valuemin="0"
            aria-valuemax="100"
            aria-valuetext={`${percent}% skills matched, ${missingPercent}% missing`}
            className="w-full h-3 rounded-full bg-[var(--bg-page)] border border-[var(--border)] overflow-hidden flex"
          >
            <div
              className="h-full bg-[var(--success)] transition-all duration-500"
              style={{ width: `${percent}%` }}
              title={`Matched: ${percent}%`}
            />
            <div
              className="h-full bg-[var(--accent-orange)] transition-all duration-500"
              style={{ width: `${missingPercent}%` }}
              title={`Missing: ${missingPercent}%`}
            />
          </div>

          <div className="flex items-center justify-between text-[11px] text-[var(--text-tertiary)] pt-0.5">
            <span className="inline-flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-[var(--success)]" aria-hidden="true" />
              <span>Matched ({matched.length})</span>
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-[var(--accent-orange)]" aria-hidden="true" />
              <span>Missing ({missing.length})</span>
            </span>
          </div>
        </div>

        {/* Two groups of pills */}
        <div className="space-y-5 pt-2 border-t border-[var(--border)]">
          {/* Group 1: Skills you already show */}
          {matched.length > 0 && (
            <div className="space-y-2.5">
              <h3 className="text-sm font-semibold text-[var(--text)] flex items-center gap-1.5">
                <Check className="w-4 h-4 text-[var(--success)]" aria-hidden="true" />
                <span>Skills you already show ({matched.length})</span>
              </h3>
              <div className="flex flex-wrap gap-2">
                {matched.map((skill, idx) => (
                  <span
                    key={`matched-${skill}-${idx}`}
                    className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[var(--success)]/10 border border-[var(--success)]/25 text-xs text-[var(--text)]"
                  >
                    <Check className="w-3.5 h-3.5 text-[var(--success)]" aria-hidden="true" />
                    <span>{skill}</span>
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Group 2: Skills the job asks for that we did not find */}
          {missing.length > 0 ? (
            <div className="space-y-2.5">
              <h3 className="text-sm font-semibold text-[var(--text)] flex items-center gap-1.5">
                <Plus className="w-4 h-4 text-[var(--accent-orange)]" aria-hidden="true" />
                <span>Skills the job asks for that we did not find ({missing.length})</span>
              </h3>
              <div className="flex flex-wrap gap-2">
                {missing.map((skill, idx) => (
                  <span
                    key={`missing-${skill}-${idx}`}
                    className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[var(--accent-orange)]/10 border border-[var(--accent-orange)]/25 text-xs text-[var(--text)]"
                  >
                    <Plus className="w-3.5 h-3.5 text-[var(--accent-orange)]" aria-hidden="true" />
                    <span>{skill}</span>
                  </span>
                ))}
              </div>
            </div>
          ) : (
            <div className="p-4 rounded-[9px] bg-[var(--success)]/10 border border-[var(--success)]/25 text-xs sm:text-sm text-[var(--success)] flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" aria-hidden="true" />
              <span>All skills from the job description are present on your resume.</span>
            </div>
          )}
        </div>

        {/* Guidance box: How to add a skill the right way */}
        <div className="p-4 sm:p-5 rounded-[10px] bg-[var(--bg-page)] border border-[var(--border)] space-y-2.5">
          <div className="flex items-center gap-2 text-xs sm:text-sm font-semibold text-[var(--brand-400)]">
            <Lightbulb className="w-4 h-4 text-[var(--brand-500)]" aria-hidden="true" />
            <span>How to add a skill the right way</span>
          </div>
          <ul className="space-y-1.5 text-xs sm:text-[13px] text-[var(--text-secondary)] leading-relaxed list-disc list-inside">
            <li>Add only skills you genuinely have.</li>
            <li>Use the job's wording.</li>
            <li>Show the skill inside a project or job bullet, not only in a list.</li>
          </ul>
        </div>
      </div>
    </section>
  );
}

