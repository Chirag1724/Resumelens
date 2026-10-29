import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Cookie, X } from 'lucide-react';

const STORAGE_KEY = 'resumelens_cookie_consent_v1';

/**
 * SitePreferences component
 * Company-standard docked bottom cookie consent banner.
 */
export default function SitePreferences() {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (!stored) {
        // Fast, smooth reveal
        const timer = setTimeout(() => setIsVisible(true), 350);
        return () => clearTimeout(timer);
      }
    } catch {
      // Fallback
    }
  }, []);

  useEffect(() => {
    // Allows opening from Footer or Settings link
    const handleOpen = () => setIsVisible(true);
    window.addEventListener('open-cookie-preferences', handleOpen);
    return () => window.removeEventListener('open-cookie-preferences', handleOpen);
  }, []);

  const handleConsent = (choice) => {
    try {
      localStorage.setItem(STORAGE_KEY, choice);
    } catch {
      // Ignore
    }
    setIsVisible(false);
  };

  return (
    <AnimatePresence>
      {isVisible && (
        <motion.div
          key="company-cookie-dock"
          initial={{ y: '100%', opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          exit={{ y: '100%', opacity: 0 }}
          transition={{ duration: 0.32, ease: [0.16, 1, 0.3, 1] }}
          className="fixed bottom-0 inset-x-0 z-50 bg-[var(--bg-card)] border-t border-[var(--border)] shadow-[0_-8px_30px_rgba(0,0,0,0.60)]"
          role="region"
          aria-label="Cookie consent banner"
        >
          <div className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-5 flex flex-col md:flex-row md:items-center justify-between gap-4">
            {/* Left: Cookie icon & Explanation */}
            <div className="flex items-start sm:items-center gap-3.5 flex-1">
              <div
                className="w-9 h-9 rounded-[9px] flex items-center justify-center text-[var(--brand-500)] shrink-0 mt-0.5 sm:mt-0"
                style={{
                  backgroundColor: 'rgba(232, 96, 46, 0.10)',
                  border: '1px solid rgba(232, 96, 46, 0.20)',
                }}
                aria-hidden="true"
              >
                <Cookie className="w-5 h-5" />
              </div>
              <div className="text-xs sm:text-sm text-[var(--text-secondary)] font-body leading-relaxed">
                <span className="font-heading font-semibold text-[var(--text)]">We use cookies.</span>{' '}
                We use cookies and essential local storage to keep your session authenticated, optimize system performance, and ensure all resume evaluations remain ephemeral and private. Learn more in our{' '}
                <Link
                  to="/privacy"
                  className="text-[var(--brand-500)] hover:text-[var(--brand-400)] hover:underline font-semibold inline-flex items-center gap-0.5 transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-[var(--brand-400)] rounded"
                >
                  Privacy Policy
                </Link>.
              </div>
            </div>

            {/* Right: Actions */}
            <div className="flex items-center gap-2.5 shrink-0 self-end md:self-auto w-full md:w-auto">
              <button
                type="button"
                onClick={() => handleConsent('essential')}
                className="flex-1 md:flex-initial px-4 py-2 sm:py-2.5 rounded-[9px] bg-[var(--bg-elevated)] hover:bg-[var(--bg-page)] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] text-xs font-semibold transition-colors cursor-pointer whitespace-nowrap text-center focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
              >
                Reject Non-Essential
              </button>

              <button
                type="button"
                onClick={() => handleConsent('accepted')}
                className="flex-1 md:flex-initial px-5 py-2 sm:py-2.5 rounded-[9px] bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] text-xs font-bold transition-all shadow-[var(--shadow-primary-hover)] cursor-pointer whitespace-nowrap text-center focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
              >
                Accept All Cookies
              </button>

              <button
                type="button"
                onClick={() => handleConsent('dismissed')}
                className="hidden md:inline-flex p-2 rounded-[9px] text-[var(--text-tertiary)] hover:text-[var(--text)] hover:bg-[var(--bg-elevated)] transition-colors cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
                aria-label="Dismiss cookie notice"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
