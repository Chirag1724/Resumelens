export default function PageBackground({ children }) {
  return (
    <div className="relative min-h-screen bg-[var(--bg-page)] text-[var(--text)] overflow-x-hidden font-sans">
      {/* Radial glow layer: warm glow at top left */}
      <div
        className="pointer-events-none fixed inset-0 z-0"
        style={{
          background: 'radial-gradient(circle at 10% 0%, rgba(232, 96, 46, 0.18) 0%, transparent 55%)',
        }}
        aria-hidden="true"
      />

      {/* Dot grid layer: radial dots spaced 24px with radial fade mask */}
      <div
        className="pointer-events-none fixed inset-0 z-0"
        style={{
          backgroundImage: 'radial-gradient(circle, rgba(255, 255, 255, 0.07) 1px, transparent 1px)',
          backgroundSize: '24px 24px',
          maskImage: 'radial-gradient(ellipse at 50% 30%, black 40%, transparent 80%)',
          WebkitMaskImage: 'radial-gradient(ellipse at 50% 30%, black 40%, transparent 80%)',
        }}
        aria-hidden="true"
      />

      {/* Diagonal hairlines layer: two faint lines at 6% opacity */}
      <svg
        className="pointer-events-none fixed inset-0 z-0 h-full w-full opacity-[0.06]"
        xmlns="http://www.w3.org/2000/svg"
        aria-hidden="true"
      >
        <line x1="0" y1="0" x2="100%" y2="100%" stroke="var(--text)" strokeWidth="1" />
        <line x1="100%" y1="15%" x2="0" y2="85%" stroke="var(--text)" strokeWidth="1" />
      </svg>

      {/* Content wrapper */}
      <div className="relative z-10 flex min-h-screen flex-col">
        {children}
      </div>
    </div>
  );
}
