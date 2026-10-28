import { FileText, Calendar, Briefcase, Clock } from 'lucide-react';

/**
 * InfoCard: Displays resume file metadata including fileName, scan date (analyzedAt),
 * targetRole, and experienceYears. Missing optional values are hidden cleanly.
 *
 * @param {Object} props
 * @param {string} props.fileName - The original file name of the analyzed resume
 * @param {string} [props.analyzedAt] - ISO timestamp when the scan was run
 * @param {string|null} [props.targetRole] - Target job role hint (optional)
 * @param {number|null} [props.experienceYears] - Candidate experience in years (optional)
 */
export default function InfoCard({
  fileName,
  analyzedAt,
  targetRole,
  experienceYears,
}) {
  const formattedDate = analyzedAt
    ? new Date(analyzedAt).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      })
    : null;

  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 flex flex-col justify-between">
      <div className="flex items-center justify-between mb-4">
        <span className="text-[11px] font-mono uppercase tracking-[0.08em] text-[var(--text-secondary)]">
          File details
        </span>
        {formattedDate && (
          <div className="inline-flex items-center gap-1.5 text-xs text-[var(--text-tertiary)]">
            <Calendar className="w-3.5 h-3.5" aria-hidden="true" />
            <span>{formattedDate}</span>
          </div>
        )}
      </div>

      <div className="flex items-start gap-3">
        <div className="w-10 h-10 rounded-[12px] bg-[rgba(232,96,46,0.10)] border border-[rgba(232,96,46,0.20)] flex items-center justify-center text-[var(--brand-500)] shrink-0">
          <FileText className="w-5 h-5" aria-hidden="true" />
        </div>
        <div className="min-w-0 flex-1">
          <h2
            className="text-base font-semibold font-heading text-[var(--text)] truncate"
            title={fileName || 'Resume Document'}
          >
            {fileName || 'Resume Document'}
          </h2>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 mt-2 text-xs text-[var(--text-secondary)]">
            {targetRole && (
              <div className="inline-flex items-center gap-1.5">
                <Briefcase className="w-3.5 h-3.5 text-[var(--text-tertiary)]" aria-hidden="true" />
                <span className="truncate">{targetRole}</span>
              </div>
            )}
            {experienceYears !== null && experienceYears !== undefined && (
              <div className="inline-flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-[var(--text-tertiary)]" aria-hidden="true" />
                <span>
                  {experienceYears} {experienceYears === 1 ? 'year' : 'years'} experience
                </span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
