/**
 * PdfIcon component
 * Inline SVG depicting a document sheet with a folded upper-right corner and an accent sparkle.
 * @param {Object} props
 * @param {string} [props.className] - Optional CSS classes
 * @param {number} [props.size] - Icon dimension in pixels (default 48)
 */
export default function PdfIcon({ className = '', size = 48 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      aria-hidden="true"
    >
      {/* Document body with folded corner */}
      <path
        d="M10 8C10 6.89543 10.8954 6 12 6H30L38 14V40C38 41.1046 37.1046 42 36 42H12C10.8954 42 10 41.1046 10 40V8Z"
        fill="var(--bg-elevated)"
        stroke="var(--border-strong)"
        strokeWidth="2"
        strokeLinejoin="round"
      />

      {/* Folded corner triangle */}
      <path
        d="M30 6V14H38"
        fill="var(--bg-card)"
        stroke="var(--border-strong)"
        strokeWidth="2"
        strokeLinejoin="round"
      />

      {/* Stylized content placeholder lines */}
      <rect x="16" y="20" width="16" height="2.5" rx="1.25" fill="var(--text-tertiary)" opacity="0.6" />
      <rect x="16" y="26" width="12" height="2.5" rx="1.25" fill="var(--text-tertiary)" opacity="0.4" />
      <rect x="16" y="32" width="14" height="2.5" rx="1.25" fill="var(--text-tertiary)" opacity="0.4" />

      {/* Small accent sparkle at top right */}
      <path
        d="M38 4L39.2 7.2L42 8L39.2 8.8L38 12L36.8 8.8L34 8L36.8 7.2L38 4Z"
        fill="var(--brand-500)"
      />
    </svg>
  );
}
