import { useState, useEffect } from 'react';

const NAV_ITEMS = [
  { id: 'overview', label: 'Overview' },
  { id: 'plan', label: 'Action plan' },
  { id: 'job-match', label: 'Job match' },
  { id: 'breakdown', label: 'Score breakdown' },
  { id: 'issues', label: 'Things to fix' },
  { id: 'details', label: 'Details' },
];

export default function ReportNav() {
  const [activeId, setActiveId] = useState('overview');

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        const visibleEntries = entries.filter((entry) => entry.isIntersecting);
        if (visibleEntries.length > 0) {
          // Sort by top position or choose the one closest to top
          const topEntry = visibleEntries.reduce((prev, curr) =>
            curr.boundingClientRect.top < prev.boundingClientRect.top ? curr : prev
          );
          setActiveId(topEntry.target.id);
        }
      },
      {
        rootMargin: '-20% 0px -65% 0px',
        threshold: [0, 0.25, 0.5],
      }
    );

    NAV_ITEMS.forEach((item) => {
      const el = document.getElementById(item.id);
      if (el) {
        observer.observe(el);
      }
    });

    return () => {
      observer.disconnect();
    };
  }, []);

  const handleClick = (e, id) => {
    e.preventDefault();
    const el = document.getElementById(id);
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
      setActiveId(id);
    }
  };

  return (
    <nav
      aria-label="Report sections"
      className="sticky top-16 sm:top-20 z-40 bg-[var(--bg-page)]/95 backdrop-blur-md border border-[var(--border)] rounded-[12px] p-2 transition-colors shadow-sm"
    >
      <div className="w-full overflow-x-auto hide-scrollbar">
        <ul className="flex items-center gap-1.5 sm:gap-2 whitespace-nowrap min-w-max">
          {NAV_ITEMS.map((item) => {
            const isActive = activeId === item.id;
            return (
              <li key={item.id}>
                <a
                  href={`#${item.id}`}
                  onClick={(e) => handleClick(e, item.id)}
                  aria-current={isActive ? 'true' : undefined}
                  className={`inline-flex items-center px-3.5 py-1.5 rounded-[9px] text-xs sm:text-[13px] font-semibold transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)] ${
                    isActive
                      ? 'bg-[var(--brand-500)] text-[var(--on-brand)] shadow-sm'
                      : 'text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--bg-card)]'
                  }`}
                >
                  {item.label}
                </a>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
}

