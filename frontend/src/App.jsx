import { useState } from 'react';
import { Routes, Route, Link, Navigate, useNavigate } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { AuthProvider, useAuth } from './context/AuthContext';
import PageBackground from './components/PageBackground';
import ErrorBoundary from './components/ErrorBoundary';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';
import ReportPage from './pages/ReportPage';
import HistoryPage from './pages/HistoryPage';
import { NotFoundPage } from './pages/Placeholders';

function TopBar() {
  const { isAuthenticated, userEmail, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleHistoryClick = (e) => {
    if (!isAuthenticated) {
      e.preventDefault();
      navigate('/login');
    }
  };

  return (
    <header className="sticky top-4 z-50 px-4 sm:px-6 w-full max-w-5xl mx-auto">
      <nav className="rounded-full bg-[var(--bg-card)] border border-[var(--border)] px-4 sm:px-6 py-2.5 flex items-center justify-between shadow-[0_20px_40px_rgba(0,0,0,0.40)]">
        {/* Left: SVG Wordmark */}
        <Link
          to="/"
          className="flex items-center gap-2 group focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] rounded-full"
        >
          <svg
            className="w-5 h-5 text-[var(--brand-500)] shrink-0 transition-transform duration-150 group-hover:scale-105"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
          >
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <circle cx="11.5" cy="14.5" r="2.5" />
            <line x1="13.3" y1="16.3" x2="16" y2="19" />
          </svg>
          <span className="font-heading font-bold text-sm tracking-tight text-[var(--text)]">
            ResumeLens
          </span>
        </Link>

        {/* Center: Desktop Navigation Links */}
        <div className="hidden md:flex items-center gap-6 text-sm font-medium">
          <Link
            to="/"
            className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] rounded-[6px]"
          >
            Home
          </Link>
          <Link
            to="/history"
            onClick={handleHistoryClick}
            className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] rounded-[6px]"
          >
            History
          </Link>
          {!isAuthenticated && (
            <Link
              to="/login"
              className="text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] rounded-[6px]"
            >
              Login
            </Link>
          )}
        </div>

        {/* Right: User Email / Auth Controls */}
        <div className="hidden md:flex items-center gap-3 text-sm">
          {isAuthenticated ? (
            <>
              <span className="text-xs text-[var(--text-secondary)] font-mono px-2.5 py-1 rounded-[6px] bg-[var(--bg-elevated)] border border-[var(--border)] max-w-[200px] truncate">
                {userEmail}
              </span>
              <button
                type="button"
                onClick={logout}
                className="px-3 py-1.5 rounded-[9px] text-xs font-semibold text-[var(--text-secondary)] hover:text-[var(--danger)] hover:bg-[rgba(239,68,68,0.12)] border border-[var(--border)] transition-colors duration-150 cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
              >
                Logout
              </button>
            </>
          ) : (
            <Link
              to="/login"
              className="px-3.5 py-1.5 rounded-full text-xs font-semibold bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] transition-colors duration-150 shadow-[var(--shadow-primary-hover)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
            >
              Login
            </Link>
          )}
        </div>

        {/* Mobile menu toggle button */}
        <div className="md:hidden flex items-center">
          <button
            type="button"
            onClick={() => setMobileMenuOpen((prev) => !prev)}
            className="p-1.5 rounded-[9px] text-[var(--text-secondary)] hover:text-[var(--text)] hover:bg-[var(--bg-elevated)] transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] cursor-pointer"
            aria-label="Toggle navigation menu"
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </nav>

      {/* Mobile menu drawer */}
      {mobileMenuOpen && (
        <div className="md:hidden mt-2 rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-4 shadow-[0_40px_80px_rgba(0,0,0,0.40)] flex flex-col gap-3">
          <Link
            to="/"
            onClick={() => setMobileMenuOpen(false)}
            className="text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors duration-150 py-1"
          >
            Home
          </Link>
          <Link
            to="/history"
            onClick={(e) => {
              setMobileMenuOpen(false);
              handleHistoryClick(e);
            }}
            className="text-sm font-medium text-[var(--text-secondary)] hover:text-[var(--text)] transition-colors duration-150 py-1"
          >
            History
          </Link>
          {isAuthenticated ? (
            <div className="pt-2 border-t border-[var(--border)] flex flex-col gap-2">
              <span className="text-xs text-[var(--text-secondary)] font-mono truncate">
                {userEmail}
              </span>
              <button
                type="button"
                onClick={() => {
                  setMobileMenuOpen(false);
                  logout();
                }}
                className="text-left text-sm font-medium text-[var(--danger)] hover:underline py-1 cursor-pointer"
              >
                Logout
              </button>
            </div>
          ) : (
            <Link
              to="/login"
              onClick={() => setMobileMenuOpen(false)}
              className="text-sm font-semibold text-[var(--brand-500)] hover:text-[var(--brand-400)] transition-colors duration-150 py-1"
            >
              Login
            </Link>
          )}
        </div>
      )}
    </header>
  );
}

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default function App() {
  return (
    <AuthProvider>
      <PageBackground>
        <TopBar />
        <main className="flex-1">
          <Routes>
            <Route
              path="/"
              element={
                <ErrorBoundary pageName="Home">
                  <LandingPage />
                </ErrorBoundary>
              }
            />
            <Route
              path="/login"
              element={
                <ErrorBoundary pageName="Login">
                  <LoginPage />
                </ErrorBoundary>
              }
            />
            <Route
              path="/report/:id"
              element={
                <ErrorBoundary pageName="Report">
                  <ReportPage />
                </ErrorBoundary>
              }
            />
            <Route
              path="/history"
              element={
                <ProtectedRoute>
                  <ErrorBoundary pageName="History">
                    <HistoryPage />
                  </ErrorBoundary>
                </ProtectedRoute>
              }
            />
            <Route
              path="*"
              element={
                <ErrorBoundary pageName="Not Found">
                  <NotFoundPage />
                </ErrorBoundary>
              }
            />
          </Routes>
        </main>
      </PageBackground>
    </AuthProvider>
  );
}
