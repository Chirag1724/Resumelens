export default function PageBackground({ children, isAuth }) {
  return (
    <div
      className={`relative ${
        isAuth ? 'h-[100dvh] max-h-[100dvh] overflow-hidden' : 'min-h-screen'
      } bg-[var(--bg-page)] text-[var(--text)] font-sans flex flex-col`}
    >
      {children}
    </div>
  );
}
