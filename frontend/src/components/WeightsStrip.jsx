import { motion } from 'framer-motion';
import { SCORING_CATEGORIES } from '../lib/scoring';

/**
 * WeightsStrip component
 * Renders the six scoring dimension chips and their configured percentage weights with smooth enter animations.
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
        {SCORING_CATEGORIES.map((category, idx) => (
          <motion.div
            key={category.sectionName}
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true, amount: 0.2 }}
            transition={{ duration: 0.45, delay: idx * 0.07, ease: [0.22, 1, 0.36, 1] }}
            whileHover={{ y: -4, transition: { duration: 0.2 } }}
            className="rounded-[6px] bg-[var(--bg-card)] border border-[var(--border)] p-3 flex flex-col items-center text-center transition-colors duration-150 hover:border-[var(--brand-500)]/40 hover:shadow-[0_10px_25px_rgba(232,96,46,0.12)] cursor-default"
          >
            <span className="font-heading font-extrabold text-lg text-[var(--brand-500)] leading-none mb-1">
              {category.weight}%
            </span>
            <span className="text-xs font-medium text-[var(--text)] line-clamp-1">
              {category.name}
            </span>
          </motion.div>
        ))}
      </div>
    </section>
  );
}
