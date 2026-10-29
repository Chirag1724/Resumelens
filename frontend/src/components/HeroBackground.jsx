export default function HeroBackground() {
  return (
    <div
      className="pointer-events-none absolute inset-0 overflow-hidden select-none z-0"
      aria-hidden="true"
    >
      {/* 1. Radial glow: warm radial glow positioned at top left */}
      <div
        className="pointer-events-none absolute inset-0 z-0"
        style={{
          background: 'radial-gradient(circle at 10% 0%, rgba(232, 96, 46, 0.18) 0%, transparent 55%)',
        }}
      />

      {/* 2. Dot grid: radial dots spaced 24px with radial fade mask */}
      <div
        className="pointer-events-none absolute inset-0 z-0"
        style={{
          backgroundImage: 'radial-gradient(circle, rgba(255, 255, 255, 0.07) 1px, transparent 1px)',
          backgroundSize: '24px 24px',
          maskImage: 'radial-gradient(ellipse at 50% 30%, black 40%, transparent 80%)',
          WebkitMaskImage: 'radial-gradient(ellipse at 50% 30%, black 40%, transparent 80%)',
        }}
      />

      {/* 3. Diagonal hairlines: two faint linear lines at 6% opacity */}
      <svg
        className="pointer-events-none absolute inset-0 z-0 h-full w-full opacity-[0.06]"
        xmlns="http://www.w3.org/2000/svg"
      >
        <line x1="0" y1="0" x2="100%" y2="100%" stroke="var(--text)" strokeWidth="1" />
        <line x1="100%" y1="15%" x2="0" y2="85%" stroke="var(--text)" strokeWidth="1" />
      </svg>
    </div>
  );
}
