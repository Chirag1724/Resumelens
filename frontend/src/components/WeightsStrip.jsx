import { SCORING_CATEGORIES } from '../lib/scoring';

/**
 * WeightsStrip component
 * Renders the six scoring dimension chips and their configured percentage weights.
 *
 * @param {Object} [props]
 * @param {string} [props.className] - Optional container CSS classes
 */
export default function WeightsStrip({ className = '' }) {
  return (
    <section className={`w-full ${className}`} aria-labelledby="weights-heading">
      <div className="text-center mb-6">
        <h2
          id="weights-heading"
          className="font-heading font-bold text-2xl sm:text-3xl text-[var(--text)] tracking-tight"
        >
          How your score is built
        </h2>
        <p className="text-sm text-[var(--text-secondary)] mt-1.5 max-w-xl mx-auto">
          Every point is computed through six transparent, rule based categories with zero opaque black boxes.
        </p>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3">
        {SCORING_CATEGORIES.map((category) => (
          <div
            key={category.sectionName}
            className="rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] p-3 flex flex-col items-center text-center transition-colors duration-150 hover:border-[var(--border-strong)]"
          >
            <span className="font-heading font-extrabold text-lg text-[var(--brand-500)] leading-none mb-1">
              {category.weight}%
            </span>
            <span className="text-xs font-medium text-[var(--text)] line-clamp-1">
              {category.name}
            </span>
          </div>
        ))}
      </div>
    </section>
  );
}
