import { Link } from 'react-router-dom';
import { RotateCcw } from 'lucide-react';

/**
 * CtaCard: Call-to-action bottom card prompting the user to re-analyze an updated resume.
 *
 * @param {Object} props
 * @param {string} [props.text='Updated your resume? Scan it again.'] - Prompting message text
 * @param {string} [props.buttonText='Re-analyze'] - Action button text
 * @param {string} [props.to='/'] - Navigation target route
 */
export default function CtaCard({
  text = 'Updated your resume? Scan it again.',
  buttonText = 'Re-analyze',
  to = '/',
}) {
  return (
    <div className="bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
      <div>
        <h3 className="font-heading font-semibold text-base text-[var(--text)]">
          {text}
        </h3>
        <p className="text-xs text-[var(--text-secondary)] mt-1">
          Upload a revised version to re-evaluate your ATS match score instantly.
        </p>
      </div>

      <Link
        to={to}
        className="inline-flex items-center gap-2 px-5 py-2.5 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-xs font-semibold transition-colors shrink-0 shadow-[var(--shadow-primary-hover)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
      >
        <RotateCcw className="w-4 h-4" aria-hidden="true" />
        <span>{buttonText}</span>
      </Link>
    </div>
  );
}
