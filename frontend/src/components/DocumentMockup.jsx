/**
 * DocumentMockup and ScanLine components
 * Renders a stylized resume document sheet with placeholder bars and skill chips.
 * When scanning is active, displays a glowing orange ScanLine traversing the document.
 *
 * @param {Object} props
 * @param {boolean} [props.scanning=false] - Whether the active scanning animation is displayed
 * @param {string} [props.className] - Optional container CSS classes
 */

export function ScanLine({ className = '' }) {
  return (
    <div
      className={`absolute left-0 right-0 h-[2px] pointer-events-none z-20 ${className}`}
      style={{
        backgroundColor: 'var(--brand-500)',
        boxShadow: '0 0 12px 2px var(--brand-500), 0 0 24px 6px rgba(232, 96, 46, 0.45)',
        animation: 'scanline-traverse 2.4s linear infinite',
      }}
      aria-hidden="true"
    />
  );
}

export default function DocumentMockup({ scanning = false, className = '' }) {
  return (
    <div
      className={`relative select-none ${className}`}
      style={{ perspective: '1000px' }}
      aria-label="Stylized document mockup"
    >
      {/* Slightly rotated white card */}
      <div
        className="relative overflow-hidden rounded-[12px] bg-white text-zinc-900 p-6 shadow-2xl border border-zinc-200 transition-transform duration-300"
        style={{
          transform: 'rotate(-1.5deg)',
          minHeight: '340px',
        }}
      >
        {/* Active ScanLine */}
        {scanning && <ScanLine />}

        {/* Mockup Header: Name & Contact info */}
        <div className="flex items-center justify-between pb-3 border-b border-zinc-200">
          <div className="space-y-1.5">
            <div className="h-4 w-28 bg-zinc-800 rounded-[6px]" />
            <div className="h-2.5 w-40 bg-zinc-400 rounded-[6px]" />
          </div>
          <div className="h-6 w-14 bg-zinc-200 rounded-[6px]" />
        </div>

        {/* Section 1: Experience */}
        <div className="mt-4 space-y-2">
          <div className="h-3 w-20 bg-zinc-700 rounded-[6px]" />
          <div className="space-y-1.5 pl-2">
            <div className="h-2 w-11/12 bg-zinc-300 rounded-[6px]" />
            <div className="h-2 w-4/5 bg-zinc-300 rounded-[6px]" />
            <div className="h-2 w-9/12 bg-zinc-300 rounded-[6px]" />
          </div>
        </div>

        {/* Section 2: Skills with small chips */}
        <div className="mt-4 space-y-2">
          <div className="h-3 w-16 bg-zinc-700 rounded-[6px]" />
          <div className="flex flex-wrap gap-1.5 pt-0.5">
            <span className="inline-block px-2 py-0.5 text-[11px] font-medium bg-zinc-100 text-zinc-700 rounded-[6px] border border-zinc-200">
              Java
            </span>
            <span className="inline-block px-2 py-0.5 text-[11px] font-medium bg-zinc-100 text-zinc-700 rounded-[6px] border border-zinc-200">
              Spring Boot
            </span>
            <span className="inline-block px-2 py-0.5 text-[11px] font-medium bg-zinc-100 text-zinc-700 rounded-[6px] border border-zinc-200">
              Docker
            </span>
            <span className="inline-block px-2 py-0.5 text-[11px] font-medium bg-zinc-100 text-zinc-700 rounded-[6px] border border-zinc-200">
              SQL
            </span>
          </div>
        </div>

        {/* Section 3: Projects */}
        <div className="mt-4 space-y-2">
          <div className="h-3 w-16 bg-zinc-700 rounded-[6px]" />
          <div className="space-y-1.5 pl-2">
            <div className="h-2 w-10/12 bg-zinc-300 rounded-[6px]" />
            <div className="h-2 w-3/4 bg-zinc-300 rounded-[6px]" />
          </div>
        </div>

        {/* Section 4: Education */}
        <div className="mt-4 space-y-2">
          <div className="h-3 w-20 bg-zinc-700 rounded-[6px]" />
          <div className="h-2 w-2/3 bg-zinc-300 rounded-[6px] pl-2" />
        </div>
      </div>
    </div>
  );
}
