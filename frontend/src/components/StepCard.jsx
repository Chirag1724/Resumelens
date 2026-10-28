/**
 * StepCard component
 * Displays a numbered sequence step for the How It Works section.
 *
 * @param {Object} props
 * @param {number|string} props.stepNumber - Step order (1, 2, 3)
 * @param {string} props.title - Step title
 * @param {string} [props.description] - Optional step summary text
 * @param {string} [props.className] - Optional container CSS classes
 */
export default function StepCard({ stepNumber, title, description, className = '' }) {
  const formattedStep = String(stepNumber).padStart(2, '0');

  return (
    <div
      className={`rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-6 flex flex-col gap-3 relative transition-all duration-300 hover:border-[var(--border-strong)] ${className}`}
    >
      <div className="flex items-center justify-between">
        <span className="font-mono text-xs font-bold text-[var(--brand-500)] tracking-widest px-2 py-0.5 rounded-[6px] bg-[rgba(232,96,46,0.10)] border border-[rgba(232,96,46,0.20)]">
          {formattedStep}
        </span>
      </div>

      <div>
        <h3 className="font-heading font-semibold text-base text-[var(--text)] tracking-tight">
          {title}
        </h3>
        {description && (
          <p className="text-sm text-[var(--text-secondary)] mt-1.5 leading-relaxed">
            {description}
          </p>
        )}
      </div>
    </div>
  );
}
