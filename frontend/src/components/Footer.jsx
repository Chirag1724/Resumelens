import { Link, useLocation } from 'react-router-dom';
import { useLenis } from 'lenis/react';
import { ArrowUp, ExternalLink } from 'lucide-react';

/**
 * Footer component
 * Minimalist, high-end open-source footer strictly following the ResumeLens theme.
 */
export default function Footer() {
  const location = useLocation();
  const lenis = useLenis();
  const isHome = location.pathname === '/';

  const scrollToTop = () => {
    if (lenis) {
      lenis.scrollTo(0, { immediate: false, duration: 0.9 });
    } else {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const scrollToUpload = () => {
    if (isHome) {
      const el = document.getElementById('upload-section');
      if (el) {
        if (lenis) {
          lenis.scrollTo(el, { immediate: false, duration: 0.8 });
        } else {
          el.scrollIntoView({ behavior: 'smooth' });
        }
        return;
      }
    }
    scrollToTop();
  };

  const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  const swaggerUrl = `${apiBase.replace(/\/api\/v1\/?$/, '').replace(/\/+$/, '')}/swagger-ui/index.html`;

  return (
    <footer className="w-full border-t border-[var(--border)] bg-[var(--bg-page)] text-[var(--text-secondary)] text-sm mt-auto relative z-20">
      <div className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 pt-16 pb-12">
        <div className="grid grid-cols-2 md:grid-cols-12 gap-8 lg:gap-12 pb-12 border-b border-[var(--border)]">
          {/* Brand Column */}
          <div className="col-span-2 md:col-span-5 space-y-3.5">
            <Link
              to="/"
              onClick={scrollToTop}
              className="inline-flex items-center gap-2.5 group focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-500)] rounded-lg"
              aria-label="ResumeLens Home"
            >
              <img
                src="/favicon.svg"
                alt="ResumeLens"
                className="w-6 h-6 object-contain rounded transition-transform duration-200 group-hover:scale-105"
              />
              <span className="font-heading font-bold text-base tracking-tight text-[var(--text)]">
                ResumeLens
              </span>
            </Link>

            <p className="text-xs sm:text-[13px] text-[var(--text-tertiary)] leading-relaxed max-w-sm">
              Explainable, open-source ATS resume intelligence. Transparent scoring, keyword gap diagnostics, and actionable LaTeX formatting fixes.
            </p>
          </div>

          {/* Product Links */}
          <div className="col-span-1 md:col-span-2 md:col-start-7 space-y-3">
            <h4 className="text-xs font-semibold text-[var(--text)] tracking-wider uppercase font-mono">
              Product
            </h4>
            <ul className="space-y-2.5 text-xs sm:text-[13px]">
              <li>
                <button
                  type="button"
                  onClick={scrollToUpload}
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors cursor-pointer text-left"
                >
                  Scanner
                </button>
              </li>
              <li>
                <Link
                  to="/"
                  onClick={scrollToTop}
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Sample Report
                </Link>
              </li>
              <li>
                <Link
                  to="/history"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  History
                </Link>
              </li>
              <li>
                <Link
                  to="/login"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Sign In
                </Link>
              </li>
            </ul>
          </div>

          {/* Resources */}
          <div className="col-span-1 md:col-span-2 space-y-3">
            <h4 className="text-xs font-semibold text-[var(--text)] tracking-wider uppercase font-mono">
              Resources
            </h4>
            <ul className="space-y-2.5 text-xs sm:text-[13px]">
              <li>
                <a
                  href="https://github.com/Chirag1724/Resumelens/blob/main/docs/ARCHITECTURE.md"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors inline-flex items-center gap-1"
                >
                  Architecture
                  <ExternalLink className="w-3 h-3 text-[var(--text-tertiary)]" />
                </a>
              </li>
              <li>
                <a
                  href={swaggerUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors inline-flex items-center gap-1"
                >
                  OpenAPI Spec
                  <ExternalLink className="w-3 h-3 text-[var(--text-tertiary)]" />
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/Chirag1724/Resumelens/blob/main/docs/SCORING_RULES.md"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Scoring Rules
                </a>
              </li>
              <li>
                <Link
                  to="/privacy"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link
                  to="/terms"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Terms of Service
                </Link>
              </li>
              <li>
                <a
                  href="https://github.com/Chirag1724/Resumelens/blob/main/SECURITY.md"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Security Policy
                </a>
              </li>
              <li>
                <button
                  type="button"
                  onClick={() => window.dispatchEvent(new CustomEvent('open-cookie-preferences'))}
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors cursor-pointer text-left"
                >
                  Cookie Preferences
                </button>
              </li>
            </ul>
          </div>

          {/* Project */}
          <div className="col-span-1 md:col-span-2 space-y-3">
            <h4 className="text-xs font-semibold text-[var(--text)] tracking-wider uppercase font-mono">
              Project
            </h4>
            <ul className="space-y-2.5 text-xs sm:text-[13px]">
              <li>
                <a
                  href="https://github.com/Chirag1724/Resumelens"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  GitHub
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/Chirag1724/Resumelens/blob/main/CONTRIBUTING.md"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Contributing
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/Chirag1724/Resumelens/blob/main/CODE_OF_CONDUCT.md"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  Code of Conduct
                </a>
              </li>
              <li>
                <a
                  href="https://github.com/Chirag1724/Resumelens/blob/main/LICENSE"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors"
                >
                  MIT License
                </a>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Sub-footer */}
        <div className="pt-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-[var(--text-tertiary)]">
          <div>
            <span>&copy; {new Date().getFullYear()} ResumeLens. Open source under the MIT License.</span>
          </div>

          <div className="flex items-center gap-2.5 sm:gap-3">
            <span className="text-[var(--border-strong)]" aria-hidden="true">&bull;</span>
            <Link
              to="/privacy"
              className="hover:text-[var(--text)] transition-colors text-xs"
            >
              Privacy
            </Link>
            <span className="text-[var(--border-strong)]" aria-hidden="true">&bull;</span>
            <Link
              to="/terms"
              className="hover:text-[var(--text)] transition-colors text-xs"
            >
              Terms
            </Link>

            <button
              type="button"
              onClick={scrollToTop}
              className="inline-flex items-center gap-1.5 text-[var(--text-tertiary)] hover:text-[var(--text)] transition-colors cursor-pointer text-xs ml-1.5 sm:ml-2"
              aria-label="Back to top"
            >
              <span>Back to top</span>
              <ArrowUp className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </footer>
  );
}
