/**
 * FeatureCard component
 * Displays a feature highlight card with a 40px rounded icon container, title, and description.
 *
 * @param {Object} props
 * @param {React.ReactNode} props.icon - Lucide line icon element
 * @param {string} props.title - Feature title
 * @param {string} props.description - Feature description
 * @param {string} [props.className] - Optional container CSS classes
 */
export default function FeatureCard({ icon, title, description, className = '' }) {
  return (
    <div
      className={`rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-6 flex flex-col gap-4 transition-all duration-300 hover:shadow-[0_0_30px_rgba(232,96,46,0.10)] hover:border-[var(--border-strong)] ${className}`}
    >
      {/* 40px rounded square icon container */}
      <div
        className="w-10 h-10 rounded-[9px] flex items-center justify-center shrink-0 text-[var(--brand-500)]"
        style={{
          backgroundColor: 'rgba(232, 96, 46, 0.10)',
          border: '1px solid rgba(232, 96, 46, 0.20)',
        }}
        aria-hidden="true"
      >
        {icon}
      </div>

      <div>
        <h3 className="font-heading font-semibold text-base text-[var(--text)] mb-1.5 tracking-tight">
          {title}
        </h3>
        <p className="text-sm text-[var(--text-secondary)] leading-relaxed">
          {description}
        </p>
      </div>
    </div>
  );
}
